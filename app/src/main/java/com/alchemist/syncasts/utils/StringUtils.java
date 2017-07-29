package com.alchemist.syncasts.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;

import com.alchemist.syncasts.R;

import java.util.Date;

public final class StringUtils {

    /**
     * Parse the time in milliseconds into String with the format: hh:mm:ss or mm:ss
     *
     * @param miliseconds The time needs to be parsed.
     */
    @SuppressLint("DefaultLocale")
    public static String formatFromMilliseconds(int miliseconds) {
        int seconds = miliseconds / 1000;
        return formatFromSeconds(seconds);
    }

    @SuppressLint("DefaultLocale")
    public static String formatFromSeconds(int seconds) {
        int minutes = seconds / 60;
        int hours = minutes / 60;
        minutes %= 60;
        seconds %= 60;
        if (hours != 0) {
            return String.format("%2d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    public static String formatDurationInMinutes(int durationInMilliseconds) {
        return (durationInMilliseconds / 1000 / 60) + " min";
    }

    public static String formatPubDate(Date pubDate, boolean breakDayLine) {
        String format = breakDayLine ? "MMM\nd" : "d MMM";
        return String.valueOf(DateFormat.format(format, pubDate));
    }

    public static String formatEpisodeTimeSize(Context context,
                                               int durationInMilliseconds,
                                               String fileSize) {
        int minutes = durationInMilliseconds / 1000 / 60;
        int hours = minutes / 60;
        minutes %= 60;
        if (hours > 0) {
            String format = context.getResources().getQuantityString(
                    R.plurals.format_hours_mins_size, hours);
            return String.format(format, hours, minutes, fileSize);
        } else {
            return String.format(context.getString(R.string.format_min_size), minutes, fileSize);
        }
    }
}