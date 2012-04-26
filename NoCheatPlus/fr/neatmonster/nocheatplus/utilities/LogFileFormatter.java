package fr.neatmonster.nocheatplus.utilities;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFileFormatter extends Formatter {

    private final SimpleDateFormat date;

    public LogFileFormatter() {
        date = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
    }

    @Override
    public String format(final LogRecord record) {
        final StringBuilder builder = new StringBuilder();
        final Throwable ex = record.getThrown();

        builder.append(date.format(record.getMillis()));
        builder.append(" [");
        builder.append(record.getLevel().getLocalizedName().toUpperCase());
        builder.append("] ");
        builder.append(record.getMessage());
        builder.append('\n');

        if (ex != null) {
            final StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            builder.append(writer);
        }

        return builder.toString();
    }
}
