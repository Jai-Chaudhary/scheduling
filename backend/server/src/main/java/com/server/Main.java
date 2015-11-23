package com.chaoxu.server;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import spark.Spark;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.chaoxu.configparser.ConfigParser;
import com.chaoxu.configparser.Config;
import com.chaoxu.library.State;
import com.chaoxu.library.Patient;
import com.chaoxu.library.PatientStat;
import com.chaoxu.library.DiscreteDistribution;
import com.chaoxu.simulator.Simulator;
import com.chaoxu.simulator.evaluator.Evaluator;

public class Main {
    private static final int step = 10;

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        mapper.readValue(
                Main.class.getClassLoader().getResource("dist.json"),
                DiscreteDistribution[].class
                );

        /**
         * request is application/www-form-urlencoded encoded:
         *  "data" field contains config string
         * response is application/json encoded:
         *  "data" field contains state string
         */
        Spark.post("/parse_synthetic", (req, res) -> {
            Config config = mapper.readValue(req.body(), Config.class);
            State state = ConfigParser.parse(config);

            res.type("application/json");
            res.header("Content-Encoding", "gzip");
            return mapper.writeValueAsString(state);
        });

        /*
        Spark.post("/get_animation_stats", (req, res) -> {
            State state = mapper.readValue(req.body(), State.class);

            Map<String, Object> data = new HashMap<>();
            data.put("animation", Simulator.simulateWithExpectation(state));
            Map<String, Double> stat = Evaluator.perPatientMean(state);
            data.put("stats", stat);

            res.type("application/json");
            return mapper.writeValueAsString(data);
        });
        */

        Spark.post("/simulate", (req, res) -> {
            State state = mapper.readValue(req.body(), State.class);

            Simulator.simulate(state, null, true);
            res.type("application/json");
            res.header("Content-Encoding", "gzip");

            return mapper.writeValueAsString(getStat(state));
        });

        Spark.post("/evaluate", (req, res) -> {
            State state = mapper.readValue(req.body(), State.class);

            Evaluator.medianStat(state);


            res.type("application/json");
            res.header("Content-Encoding", "gzip");

            return "hello";
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
            Simulator.simulate(state, 1, true);
            Simulator.simulateTick(state);

            List<Map<String, Object>> frames = new ArrayList<>();
            frames.add(getFrame(state));
            while (true) {
                boolean reachStopTime = Simulator.simulate(state,
                        state.time + step, true);
                frames.add(getFrame(state));
                if (!reachStopTime) {
                    break;
                }
            }

            res.type("application/json");
            // compress response so the transfering is lighting fast
            res.header("Content-Encoding", "gzip");
            return mapper.writeValueAsString(frames);
        });
    }

    private static List<PatientStat> getStat(State state) {
        List<PatientStat> ret = new ArrayList<>();
        for (Patient p : state.patients)
            ret.add(new PatientStat(p));
        return ret;
    }

    private static Map<String, Object> getFrame(State state) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("time", state.time);
        ret.put("animation", getStat(Simulator.simulateWithMedian(state)));
        ret.put("stats", Evaluator.medianStat(state));
        return ret;
    }
}
