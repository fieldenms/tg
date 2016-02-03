package ua.com.fielden.platform.web.centre.api.resultset.toolbar;

import ua.com.fielden.platform.dom.CssStyles;
import ua.com.fielden.platform.web.interfaces.IImportable;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.minijs.JsCode;

public interface IToolbarConfig extends IRenderable, IImportable {

    /**
     * Generates the Java script code for this toolbar depending on entity type.
     *
     * @param entityType
     *            - the entity type for entity centre that contains this toolbar.
     * @return
     */
    JsCode code(final Class<?> entityType);

    /**
     * Returns the css styles needed for this toolbar.
     *
     * @return
     */
    CssStyles styles();

}
