package com.myfinal.objectengine.common;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 时间格式化：yyyy-MM-dd HH:mm:ss
 */
public final class DateUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateUtils() {
    }

    public static String format(Date date) {
        return date == null ? null : FORMATTER.withZone(ZoneId.systemDefault()).format(date.toInstant());
    }
}
