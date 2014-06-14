package ua.com.fielden.platform.web.gis;

import javafx.application.Platform;
import javafx.scene.web.WebView;

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

    public void exit() {
        Platform.exit();
    }

    public void println(final String string) {
        System.out.println(string);
    }
}
