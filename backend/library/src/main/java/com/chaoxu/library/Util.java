package com.chaoxu.library;

public class Util {
    public static String toTime(int t) {
        return String.format("%02d:%02d", t / 60, t % 60);
    }
}
