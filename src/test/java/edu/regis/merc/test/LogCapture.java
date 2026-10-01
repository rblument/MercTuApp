package edu.regis.merc.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/** Captures JUL and direct console output; restores global state on close. */
public final class LogCapture implements AutoCloseable {
    public final List<LogRecord> records = new ArrayList<>();
    private final Logger logger;
    private final Level previousLevel;
    private final boolean previousParentHandlers;
    private final PrintStream previousOut = System.out;
    private final PrintStream previousErr = System.err;
    private final ByteArrayOutputStream console = new ByteArrayOutputStream();
    private final PrintStream stream = new PrintStream(console, true, StandardCharsets.UTF_8);
    private final Handler handler = new Handler() {
        @Override public void publish(LogRecord record) { records.add(record); }
        @Override public void flush() {}
        @Override public void close() {}
    };

    public LogCapture(Class<?> source) {
        logger = Logger.getLogger(source.getName());
        previousLevel = logger.getLevel();
        previousParentHandlers = logger.getUseParentHandlers();
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        System.setOut(stream);
        System.setErr(stream);
    }

    public String output() {
        SimpleFormatter formatter = new SimpleFormatter();
        StringBuilder output = new StringBuilder(console.toString(StandardCharsets.UTF_8));
        records.forEach(record -> output.append(formatter.format(record)));
        return output.toString();
    }

    @Override public void close() {
        System.setOut(previousOut);
        System.setErr(previousErr);
        logger.removeHandler(handler);
        logger.setLevel(previousLevel);
        logger.setUseParentHandlers(previousParentHandlers);
        stream.close();
    }
}
