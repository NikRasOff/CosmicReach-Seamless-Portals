package com.nikrasoff.seamlessportals.extras;

public class SomeStringUtils {
    // After all, why not?
    // Why shouldn't I create a whole separate class for just this one function?
    public static boolean isValidFloat(String s){
        return s.matches("[+-]?(?:0|[123456789]\\d*)\\.?\\d*");
    }
}
