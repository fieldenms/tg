package ua.com.fielden.platform.web.gis;

import javafx.application.Platform;
import javafx.scene.web.WebView;

import org.apache.log4j.Logger;

/**
 * A bridge between JavaScript and Java for {@link WebView} web application integration. Allows to use Java code from JavaScript. Usage:
 * <p>
 * <code>
        final JSObject jsobj = (JSObject) webEngine.executeScript("window");<br>
        jsobj.setMember("java", new Js2JavaBridge());<br><br>
 * </code> and then:<br>
 * <p>
 * <code>
        java.println(javaScriptObj);
 * </code>
 *
 * @author TG Team
 *
 */
public class Js2JavaBridge {
    private final Logger logger = Logger.getLogger(getClass());

    public void exit() {
        Platform.exit();
    }

    public void println(final String string) {
        System.out.println(string);
    }

    public void info(final String string) {
        logger.info(string);
    }

    public void selectTabularlyWithoutEventFiring(final String featureId) {
        System.out.println("selectTabularlyWithoutEventFiring(" + featureId + ");");
    }
}
