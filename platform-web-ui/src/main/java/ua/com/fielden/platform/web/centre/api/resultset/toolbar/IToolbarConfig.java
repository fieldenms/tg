package ua.com.fielden.platform.web.centre.api.resultset.toolbar;

import java.util.List;

import ua.com.fielden.platform.dom.CssStyles;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.web.interfaces.IImportable;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.minijs.JsCode;

/**
 * Configuration for toolbars available for entity centres, insertion points and alternative views.
 * 
 * @author TG Team
 *
 */
public interface IToolbarConfig extends IRenderable, IImportable {

    public static final InnerTextElement topLevelPlacement = new InnerTextElement("<!-- GENERATED FUNCTIONAL ACTIONS: -->\n<!--@functional_actions-->");
    public static final InnerTextElement switchViewPlacement = new InnerTextElement("<!-- GENERATED SWITCH VIEW ACTIONS: -->\n<!--@switch_view_actions-->");

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

    /**
     * Returns the list of available shortcuts for present actions.
     *
     * @return
     */
    List<String> getAvailableShortcuts();

    /**
     * Returns the size of switch view button
     *
     * @return
     */
    int getSwitchViewButtonWidth();

}