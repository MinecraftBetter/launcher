package fr.minecraftbetter.launcher.utils.logging;

import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class LogFormatter extends SimpleFormatter {
    private String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

    public LogFormatter() {}

    public LogFormatter(String format) {this.format = format;}


    @Override
    public synchronized String format(LogRecord lr) {
        return String.format(format, new Date(lr.getMillis()), lr.getLevel().getLocalizedName(), lr.getMessage());
    }
}
