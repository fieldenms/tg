package ua.com.fielden.platform.web.minijs;

/**
 * Represents the base abstraction for Java Script object.
 *
 * @author TG Team
 *
 */
public class JsCode {

    private final String jsCode;

    /**
     * A convenient factory method to avoid {@code new JsCode(...)}.
     *
     * @param code -- a snippet of JS code.
     * @return an instance of {@code JsCode}.
     */
    public static JsCode jsCode(final String code) {
    	return new JsCode(code);
    }
    
    /**
     * Creates new {@link JsCode} with specified java script code.
     *
     * @param jsCode
     */
    public JsCode(final String jsCode) {
        this.jsCode = jsCode;
    }

    @Override
    public String toString() {
        return jsCode;
    }
}
