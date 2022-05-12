package ua.com.fielden.platform.processors.metamodel;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class ProcessorLogger {
    private String filename;
    private String source;
    private Logger logger;
    private FileWriter fileWriter;
    private PrintWriter printWriter;
    
    public ProcessorLogger(String filename, String source, Logger logger) {
        this.filename = filename;
        this.source = source;
        this.logger = logger;
        try {
            this.fileWriter = new FileWriter(filename, true);
        } catch (IOException e) {
            this.printWriter = null;
            return;
        }
        this.printWriter = new PrintWriter(fileWriter);
    }
    
    private String getDatetime() {
        final DateTimeZone zone = DateTimeZone.forID("Europe/Kiev");
        final DateTime now = DateTime.now(zone);
        String datetime = now.toString("yyyy-MM-dd HH:mm:ss.SSS@z");

        return datetime;
    }

    public boolean log(String text, String level) {
        if (printWriter == null) 
            return false;

        String datetime = getDatetime();
        printWriter.println(String.format("%s [%5s] [%s] --- %s", datetime, level, source, text));
        return true;
    }
    
    public boolean debug(String text) {
        if (logger != null)
            logger.debug(text);

        return log(text, "DEBUG");
    }

    public boolean info(String text) {
        if (logger != null)
            logger.info(text);

        return log(text, "INFO");
    }

    public boolean error(String text) {
        if (logger != null)
            logger.error(text);

        return log(text, "ERROR");
    }

    public boolean ln() {
        if (printWriter == null)
            return false;
        printWriter.println();
        return true;
    }
    
    public boolean end() {
        if (printWriter == null)
            return false;
        printWriter.close();
        return true;
    }
}
