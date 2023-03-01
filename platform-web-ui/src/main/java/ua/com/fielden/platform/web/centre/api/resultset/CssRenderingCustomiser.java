package ua.com.fielden.platform.web.centre.api.resultset;

import java.util.Collections;
import java.util.Map;

/**
 * An abstract base class for implementation of CSS rendering customisers, used by the Entity Centre DSL.
 *
 * @see IRenderingCustomiser
 * @author TG Team
 */
public abstract class CssRenderingCustomiser implements IRenderingCustomiser<Map<String, Object>> {

    protected static final String BACKGROUND_STYLES = "backgroundStyles";
    protected static final String VALUE_STYLES = "valueStyles";

    private static final Map<String, Object> EMPTY_HINTS = Collections.emptyMap();

    @Override
    public final Map<String, Object> empty() {
        return EMPTY_HINTS;
    }

}
