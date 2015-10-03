package com.chaoxu.cli;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.chaoxu.library.Blob;
import com.chaoxu.simulator.Simulator;
import com.chaoxu.configparser.Config;
import com.chaoxu.configparser.ConfigParser;

public class Main {
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Blob blob;

        if (args.length > 0 && args[0].equals("blob")) {
            blob = mapper.readValue(System.in, Blob.class);
        } else {
            Config config = mapper.readValue(System.in, Config.class);
            // System.out.println(mapper.writeValueAsString(config));

            blob = ConfigParser.parse(config);
            // System.out.println(mapper.writeValueAsString(state));
        }

        Simulator.simulate(blob.state, blob.lBits, null, true);
    }
}
