package com.chaoxu.server;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import spark.Spark;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.chaoxu.configparser.ConfigParser;
import com.chaoxu.configparser.Config;
import com.chaoxu.library.State;
import com.chaoxu.simulator.Simulator;
import com.chaoxu.simulator.Evaluator;

public class Main {
    private static final int step = 10;

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        /**
         * request is application/www-form-urlencoded encoded:
         *  "data" field contains config string
         * response is application/json encoded:
         *  "data" field contains state string
         */
        Spark.post("/parse_synthetic", (req, res) -> {
            // System.out.println(req.body());

            Config config = mapper.readValue(req.body(), Config.class);
            State state = ConfigParser.parse(config);

            res.type("application/json");
            res.header("Content-Encoding", "gzip");
            return mapper.writeValueAsString(state);
        });

        Spark.post("/get_animation_stats", (req, res) -> {
            State state = mapper.readValue(req.body(), State.class);

            Map<String, Object> data = new HashMap<>();
            data.put("animation", Simulator.simulateWithExpectation(state));
            Map<String, Double> stat = Evaluator.perPatientMean(state);
            data.put("stats", stat);

            res.type("application/json");
            return mapper.writeValueAsString(data);
        });

        Spark.post("/simulate", (req, res) -> {
            State state = mapper.readValue(req.body(), State.class);

            Simulator.simulate(state, null, true);
            res.type("application/json");
            res.header("Content-Encoding", "gzip");
            return mapper.writeValueAsString(state);
        });

        /**
         * This will return a list of frames, each frame contains:
         *      state: the state of that frame
         *      animation: the simulation with mean for animation purpose
         *      stats: the statistics about each patients waiting time
         *
         * request is application/www-form-urlencoded encoded:
         *  "step" field is step-size
         *  "data" field contains state string
         * response is application/json encoded
         *  "data" field contains string for frames
         */
        Spark.post("/simulate_frames", (req, res) -> {
            State state = mapper.readValue(req.body(), State.class);
            Simulator.simulateTick(state);

            List<Map<String, Object>> frames = new ArrayList<>();
            frames.add(new HashMap<String, Object>() {{
                put("state", new State(state));
                put("animation", Simulator.simulateWithExpectation(state));
                put("stats", Evaluator.perPatientMean(state));
            }});

            while (true) {
                boolean reachStopTime = Simulator.simulate(state,
                        state.time + step, true);
                frames.add(new HashMap<String, Object>() {{
                    put("state", new State(state));
                    put("animation", Simulator.simulateWithExpectation(state));
                    put("stats", Evaluator.perPatientMean(state));
                }});
                if (!reachStopTime) {
                    break;
                }
            }

            Map<String, Object> ret = new HashMap<>();
            ret.put("data", frames);
            res.type("application/json");
            // compress response so the transfering is lighting fast
            res.header("Content-Encoding", "gzip");
            return mapper.writeValueAsString(ret);
        });
    }
}
