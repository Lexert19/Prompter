package com.example.promptengineering.staticUtils;

import java.util.Random;
public class RandomUtils {
    private static Random random = new Random();

    public static long getLong(){
        return random.nextLong();
    }
}
