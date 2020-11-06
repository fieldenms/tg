package ua.com.fielden.platform.web.centre.api.actions.multi;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.crit.impl.AbstractCriterionWidget;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IImportable;
import ua.com.fielden.platform.web.interfaces.IRenderable;

public class FunctionalMultiActionElement implements IRenderable, IImportable {

    private final String widgetName;
    private final String widgetPath;

    private final List<FunctionalActionElement> actionElements = new ArrayList<>();

    public FunctionalMultiActionElement(final EntityMultiActionConfig entityMultiActionConfig, final int numberOfAction, final FunctionalActionKind actionKind) {
        this.widgetPath = "egi/tg-egi-multi-action";
        this.widgetName = AbstractCriterionWidget.extractNameFrom(this.widgetPath);

        for (int configIndex = 0; configIndex < entityMultiActionConfig.actions().size(); configIndex++) {
            final EntityActionConfig entityActionConfig = entityMultiActionConfig.actions().get(configIndex);
            if (!entityActionConfig.isNoAction()) {
                actionElements.add(new FunctionalActionElement(entityActionConfig, numberOfAction + configIndex, actionKind));
            }
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
     * Creates a string representation for the object which holds pre- and post-actions.
     *
     * @return
     */
    public String createActionObject(final LinkedHashSet<String> importPaths) {
        final String prefix = ",\n";
        final StringBuilder actionsObjects = new StringBuilder();
        for(final FunctionalActionElement el: actionElements) {
            importPaths.add(el.importPath());
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
