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
    };
}
