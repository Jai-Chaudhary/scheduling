package com.chaoxu.library;

import java.util.Map;
import java.util.HashMap;

/**
 * RandomBits class stores uniform random
 * variables to generate duration and lateness.
 * This is used for common random number to
 * reduce variance when evaluating state.
 */
public class RandomBits {
    public Map<String, Double> duration = new HashMap<>();
    public Map<String, Double> lateness = new HashMap<>();
}
