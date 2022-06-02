package fr.minecraftbetter.launcher.utils.logging;

import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class LogFormatter extends SimpleFormatter {
    private static final String FORMAT = "[%1$tF %1$tT] [%2$-7s] %3$s%4$s %n";

    @Override
    public synchronized String format(LogRecord lr) {
        String trace = lr.getThrown() == null ? "" : MessageFormat.format(", {0} ({1})", lr.getThrown().getMessage(), lr.getThrown().getCause());
        return String.format(FORMAT, new Date(lr.getMillis()), lr.getLevel().getLocalizedName(), lr.getMessage(), trace);
    }
}
