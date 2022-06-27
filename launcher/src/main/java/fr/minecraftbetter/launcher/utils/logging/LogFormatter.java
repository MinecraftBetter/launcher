package fr.minecraftbetter.launcher.utils.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class LogFormatter extends SimpleFormatter {
    private static final String FORMAT = "[%1$tF %1$tT] [%2$-7s] %3$s%4$s %n";

    @Override
    public synchronized String format(LogRecord lr) {
        Throwable thrown = lr.getThrown();
        ArrayList<String> traces = new ArrayList<>();
        while (thrown != null){
            traces.add(("→ " + "\t".repeat(traces.size() + 1)) + thrown.getMessage()
                    + "\n\t" + ("\t".repeat(traces.size() + 1))
                    + String.join("\n\t" + ("\t".repeat(traces.size() + 1)), Arrays.stream(thrown.getStackTrace()).map(StackTraceElement::toString).toList()));
            thrown = thrown.getCause();
        }
        String trace = traces.isEmpty() ? "" : "\n" + String.join("\n", traces);
        return String.format(FORMAT, new Date(lr.getMillis()), lr.getLevel().getName(), lr.getMessage(), trace);
    }
}
