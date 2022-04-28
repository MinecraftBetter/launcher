package fr.minecraftbetter.launcher.utils.logging;

import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class LogFormatter extends SimpleFormatter {
    private String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";
    private String formatStackTrace = "[%1$tF %1$tT] [%2$-7s] %3$s, %4$s %n";

    public LogFormatter() {}

    public LogFormatter(String format, String formatStackTrace) {
        this.format = format;
        this.formatStackTrace = format;
    }


    @Override
    public synchronized String format(LogRecord lr) {
        return String.format(lr.getThrown() == null ? format : formatStackTrace,
                new Date(lr.getMillis()), lr.getLevel().getLocalizedName(), lr.getMessage(), lr.getThrown());
    }
}
