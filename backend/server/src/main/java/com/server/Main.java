package com.chaoxu.server;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import spark.Spark;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.chaoxu.library.State;
import com.chaoxu.library.Blob;
import com.chaoxu.library.Patient;
import com.chaoxu.configparser.ConfigParser;
import com.chaoxu.configparser.Config;
import com.chaoxu.simulator.Simulator;
import com.chaoxu.simulator.Evaluator;

class SimulateFramesReq {
    public String data;
    public int step;
}

class ConfigParserReq {
    public String data;
}

public class Main {
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Spark.staticFileLocation("/public");

        /**
         * request is application/www-form-urlencoded encoded:
         *  "data" field contains config string
         * response is application/json encoded:
         *  "data" field contains state string
         */
        Spark.post("/configparser", (req, res) -> {
            // System.out.println(req.body());

            ConfigParserReq r = mapper.readValue(req.body(), ConfigParserReq.class);
            Config config = mapper.readValue(r.data,
                    Config.class);
            Blob blob = ConfigParser.parse(config);
            State state = blob.state;

            String blobStr = mapper.writeValueAsString(blob);

            Map<String, Object> ret = new HashMap<>();
            ret.put("data", blobStr);
            res.type("application/json");
            res.header("Content-Encoding", "gzip");
            return mapper.writeValueAsString(ret);
        });

        Spark.post("/get_animation_stats", (req, res) -> {
            Blob blob = mapper.readValue(req.body(), Blob.class);
            State state = blob.state;

            Map<String, Object> data = new HashMap<>();
            data.put("animation", Simulator.simulateWithExpectation(state));
            data.put("stats", Evaluator.perPatientMean(state, blob.lBits));

            Map<String, Object> ret = new HashMap<>();
            ret.put("data", mapper.writeValueAsString(data));

            res.type("application/json");
            return mapper.writeValueAsString(ret);
        });

        Spark.post("/simulate", (req, res) -> {
            Blob blob = mapper.readValue(req.body(), Blob.class);
            State state = blob.state;

            Simulator.simulate(state, blob.lBits, null, true);
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
            SimulateFramesReq r = mapper.readValue(req.body(), SimulateFramesReq.class);
            int step = r.step;
            // System.out.println(r.data);
            Blob blob = mapper.readValue(r.data, Blob.class);
            State state = blob.state;
            Simulator.simulateTick(state);

            List<Map<String, Object>> frames = new ArrayList<>();
            frames.add(new HashMap<String, Object>() {{
                put("state", new State(state));
                put("animation", Simulator.simulateWithExpectation(state));
                put("stats", Evaluator.perPatientMean(state, blob.lBits));
            }});

            while (true) {
                boolean reachStopTime = Simulator.simulate(state, blob.lBits,
                        state.time + step, true);
                frames.add(new HashMap<String, Object>() {{
                    put("state", new State(state));
                    put("animation", Simulator.simulateWithExpectation(state));
                    put("stats", Evaluator.perPatientMean(state, blob.lBits));
                }});
                if (!reachStopTime) {
                    break;
                }
            }

            Map<String, Object> ret = new HashMap<>();
            ret.put("data", mapper.writeValueAsString(frames));
            res.type("application/json");
            // compress response so the transfering is lighting fast
            res.header("Content-Encoding", "gzip");
            return mapper.writeValueAsString(ret);
        });
    }
}
