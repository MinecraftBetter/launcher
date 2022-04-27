package fr.minecraftbetter.launcher.utils.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public class OutConsoleHandler extends StreamHandler {

    @SuppressWarnings("java:S106")
    public OutConsoleHandler() {super(System.out, new SimpleFormatter());}

    @SuppressWarnings("java:S106")
    public OutConsoleHandler(Formatter format) {super(System.out, format);}

    @Override
    public synchronized void publish(LogRecord logRecord) {
        super.publish(logRecord);
        super.flush();
    }

    @Override
    public synchronized void close() {
        super.flush();
    }
}
