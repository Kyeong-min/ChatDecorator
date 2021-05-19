package me.htna.project.chatdecorator.placeholderHandlers;

import me.htna.project.chatdecorator.ChatDecorator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public abstract class BasePlaceholderHandler implements IPlaceholderHandler {

    protected BasePlaceholderHandler() {

    }

    /**
     * Get datetime format string
     *
     * @param instant instant
     * @param format format string
     * @return datetime format string
     */
    public static String getDateTimeString(Instant instant, String format) {
        if (instant == null)
            return null;

        ChatDecorator plugin = ChatDecorator.getInstance();
        DateTimeFormatter dateTimePattern;
        try {
            dateTimePattern = DateTimeFormatter.ofPattern(format);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().error("Invalid DateTime format: " + format);
            plugin.getLogger().error("Use default datetime format");
            dateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        }

        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(dateTimePattern);
    }

    /**
     * Get date format string
     * @param instant instant
     * @param format format string
     * @return date format string
     */
    public static String getDateString(Instant instant, String format) {
        if (instant == null)
            return null;

        ChatDecorator plugin = ChatDecorator.getInstance();
        DateTimeFormatter dateTimePattern;
        try {
            dateTimePattern = DateTimeFormatter.ofPattern(format);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().error("Invalid date format: " + format);
            plugin.getLogger().error("Use default date format");
            dateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        }

        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(dateTimePattern);
    }

    /**
     * Get time format string
     * @param instant instant
     * @param format format string
     * @return time format string
     */
    public static String getTimeString(Instant instant, String format) {
        if (instant == null)
            return null;

        ChatDecorator plugin = ChatDecorator.getInstance();
        DateTimeFormatter dateTimePattern;
        try {
            dateTimePattern = DateTimeFormatter.ofPattern(format);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().error("Invalid time format: " + format);
            plugin.getLogger().error("Use default time format");
            dateTimePattern = DateTimeFormatter.ofPattern("HH:mm:ss");
        }

        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(dateTimePattern);
    }

    /**
     * Get now datetime format string
     * @param format format string
     * @return datetime format string
     */
    public static String getNowDateTimeString(String format) {
        return getDateTimeString(Instant.now(), format);
    }

    /**
     * Get now date format string
     * @param format format string
     * @return date format string
     */
    public static String getNowDateString(String format) {
        return getDateString(Instant.now(), format);
    }

    /**
     * Get now time format string
     * @param format format string
     * @return time format string
     */
    public static String getNowTimeString(String format) {
        return getTimeString(Instant.now(), format);
    }
}
