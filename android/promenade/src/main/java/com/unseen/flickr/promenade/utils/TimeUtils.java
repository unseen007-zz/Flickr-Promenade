package com.unseen.flickr.promenade.utils;

/**
 * Copyright 2014 Steven Perraudin
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class TimeUtils {

    /**
     * Converts a duration into a human readable String
     * @param duration the duration
     * @return a human readable String
     */
    public static String convertDurationtoString(long duration) {
        int hour = (int) (duration / 3600000);
        int min = (int) ((duration - (hour * 3600000)) / 60000);
        int sec = (int) ((duration - (hour * 3600000) - (min * 60000)) / 1000);
        StringBuilder builder = new StringBuilder();
        builder.append(hour).append("h");
        builder.append(firstDigit(min));
        if (sec != 0)
            builder.append("m").append(firstDigit(sec)).append("s");

        return builder.toString();
    }

    /**
     * Adds a 0 to number below 10
     * @param min the number
     * @return the generated String
     */
    private static String firstDigit(int min) {
        if (min < 10) {
            return "0" + String.valueOf(min);
        }
        return String.valueOf(min);
    }

}
