package com.tryfit;

/**
 * Created by alexeyreznik on 19/05/2017.
 */
public class Utilities {

    public static String convertToRusSize(int stickLength) {

        String result = "Men RUS size: ";

        if (stickLength < 250) {

            result += "38";
        } else if (stickLength < 255) {
            result += "39";
        } else if (stickLength < 260) {
            result += "40";
        } else if (stickLength < 265) {
            result += "40.5";
        } else if (stickLength < 270) {
            result += "41";
        } else if (stickLength < 275) {
            result += "42";
        } else if (stickLength < 280) {
            result += "43";
        } else if (stickLength < 285) {
            result += "43.5";
        } else if (stickLength < 290) {
            result += "44";
        } else if (stickLength < 295) {
            result += "45";
        } else if (stickLength < 300) {
            result += "46";
        }

        return result;
    }
}
