package com.chaoxu.library;

import java.util.List;

/**
 * Blob contains list of bits and state. This
 * is everything you need for simulation,
 * state represents the current status and
 * lBits provides all bits you need to sample
 * distributions. Each RandomBits contains
 * bits you need to sample all distribution once.
 */
public class Blob {
    public List<RandomBits> lBits;
    public State state;
}
