package com.defano.hypercard.util;

public class NumberUtils {

    public static int range(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

}
