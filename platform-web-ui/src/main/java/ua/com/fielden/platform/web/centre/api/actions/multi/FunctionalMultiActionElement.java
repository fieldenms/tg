package ua.com.fielden.platform.web.centre.api.actions.multi;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.crit.impl.AbstractCriterionWidget;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IImportable;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.minijs.JsImport;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedSet;

import static ua.com.fielden.platform.web.minijs.JsImport.extendAndValidateCombinedImports;

/**
 * {@link IRenderable} and {@link IImportable} element that represents multiple action element and it is renderable into tg-egi-multi-action component instance.
 *
 * @author TG Team
 *
 */
public class FunctionalMultiActionElement implements IRenderable, IImportable {

    private final String widgetName;
    private final String widgetPath;

    private final List<FunctionalActionElement> actionElements = new ArrayList<>();

    /**
     * Creates new muti action element based on config, action number and it's kind. This element is rendered into tg-egi-multi-action with set of simple tg-ui-action inside shadow dom.
     *
     * @param entityMultiActionConfig
     * @param numberOfAction
     * @param actionKind
     */
    public FunctionalMultiActionElement(final EntityMultiActionConfig entityMultiActionConfig, final int numberOfAction, final FunctionalActionKind actionKind) {
        this.widgetPath = "egi/tg-egi-multi-action";
        this.widgetName = AbstractCriterionWidget.extractNameFrom(this.widgetPath);

        for (int configIndex = 0; configIndex < entityMultiActionConfig.actions().size(); configIndex++) {
            final EntityActionConfig entityActionConfig = entityMultiActionConfig.actions().get(configIndex);
            actionElements.add(new FunctionalActionElement(entityActionConfig, numberOfAction + configIndex, actionKind));
        }
    }

    @Override
    public DomElement render() {
        final DomElement multiActionElement = new DomElement(widgetName);
        for(final FunctionalActionElement el: actionElements) {
            multiActionElement.add(el.render().attr("slot", "multi-action-item"));
        }
        return multiActionElement;
    }

    /**
     * Creates a string representation for the object which holds attributes, pre- and post-actions.
     *
     * @return
     */
    public String createActionObject(final LinkedHashSet<String> importPaths, final SortedSet<JsImport> actionImports) {
        final String prefix = ",\n";
        final StringBuilder actionsObjects = new StringBuilder();
        for(final FunctionalActionElement el: actionElements) {
            importPaths.add(el.importPath());
            extendAndValidateCombinedImports(actionImports, el.actionImports());
            actionsObjects.append(prefix + el.createActionObject());
        }
        final int prefixLength = prefix.length();
        final String actionString = actionsObjects.toString();
        return actionString.length() > prefixLength ? actionString.substring(prefixLength) : actionString;
    }

    @Override
    public String importPath() {
        return widgetPath;
    }

}
