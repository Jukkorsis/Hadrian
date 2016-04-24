package com.northernwall.hadrian.utilityHandlers;

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
        writeln("<tr><td>" + label + "</td><td>" + value + "</td></tr>");
    }

    public void addLine(String label, Date value) throws IOException {
        HealthWriter.this.addLine(label, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value));
    }

    public void addLine(String label, int value) throws IOException {
        HealthWriter.this.addLine(label, Integer.toString(value));
    }

}
