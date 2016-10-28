package com.northernwall.hadrian.handlers.utility;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

public class HealthWriter {

    private final OutputStream stream;

    public HealthWriter(OutputStream stream) {
        this.stream = stream;
    }

    private void writeln(String text) throws IOException {
        stream.write(text.getBytes());
    }

    public void open() throws IOException {
        writeln("<html>");
        writeln("<body>");
        writeln("<table>");
    }

    public void close() throws IOException {
        writeln("</table>");
        writeln("</body>");
        writeln("</html>");
        stream.close();
    }

    public void addStringLine(String label, String value) throws IOException {
        if (value == null) {
            writeln("<tr><td>" + label + "</td><td>-- NULL --</td></tr>");
        } else if (value.isEmpty()) {
            writeln("<tr><td>" + label + "</td><td>-- EMPTY --</td></tr>");
        } else {
            writeln("<tr><td>" + label + "</td><td>" + value + "</td></tr>");
        }
    }

    public void addDateTimeLine(String label, OffsetDateTime value) throws IOException {
        if (value == null) {
            addStringLine(label, null);
        } else {
            addStringLine(label, value.toString() + " or " + value.atZoneSameInstant(ZoneId.of("UTC")).toString());
        }
    }

    public void addDateLine(String label, Date value) throws IOException {
        if (value == null) {
            addStringLine(label, null);
        } else {
            addStringLine(label, new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ").format(value));
        }
    }

    public void addIntLine(String label, int value) throws IOException {
        addStringLine(label, Integer.toString(value));
    }

    void addClassLine(String label, Object value) throws IOException {
        if (value == null) {
            addStringLine(label, null);
        } else {
            addStringLine(label, value.getClass().getCanonicalName());
        }
    }

}
