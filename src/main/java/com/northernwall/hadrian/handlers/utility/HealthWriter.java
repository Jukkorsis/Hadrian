package com.northernwall.hadrian.handlers.utility;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
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

    public void addLine(String label, String value) throws IOException {
        if (value == null) {
            writeln("<tr><td>" + label + "</td><td>-- NULL --</td></tr>");
        } else if (value.isEmpty()) {
            writeln("<tr><td>" + label + "</td><td>-- EMPTY --</td></tr>");
        } else {
            writeln("<tr><td>" + label + "</td><td>" + value + "</td></tr>");
        }
    }

    public void addLine(String label, Date value) throws IOException {
        if (value == null) {
            HealthWriter.this.addLine(label, (String)null);
        } else {
            HealthWriter.this.addLine(label, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value));
        }
    }

    public void addLine(String label, int value) throws IOException {
        HealthWriter.this.addLine(label, Integer.toString(value));
    }

    void addClassLine(String label, Object value) throws IOException {
        if (value == null) {
            HealthWriter.this.addLine(label, (String)null);
        } else {
            HealthWriter.this.addLine(label, value.getClass().getCanonicalName());
        }
    }

}
