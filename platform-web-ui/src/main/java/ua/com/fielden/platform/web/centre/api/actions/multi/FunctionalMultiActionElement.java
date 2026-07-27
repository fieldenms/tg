package ua.com.fielden.platform.web.centre.api.actions.multi;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.crit.impl.AbstractCriterionWidget;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IImportable;
import ua.com.fielden.platform.web.interfaces.IRenderable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/// Server-side representation of a single multi-action group on a centre.
/// A group corresponds to one `withAction(...)`, `withMultiAction(...)` or `withActionSupplier(...)` call in the Entity Centre DSL — including the trivial `SingleActionSelector`-wrapped form produced by `withAction(...)`.
/// Renders into a `tg-egi-multi-action` element whose `multi-action-item` slot holds one `tg-ui-action` per sub-action of the group.
///
/// Used by primary actions on a centre's row (one group per row), secondary actions on a centre's row (one group per declared secondary action), and property actions on EGI columns (one group per `withAction` / `withMultiAction` call on the column).
/// `numberOfAction` is the running offset into the centre's flat action index space — each sub-action of the group is assigned `numberOfAction + i`; group boundaries are preserved at the rendering level so the client-side selector for the group can pick a sub-action by relative index.
///
/// This class is agnostic of the column-level `chosen-property` concern.
/// For property-action groups, [ua.com.fielden.platform.web.centre.api.resultset.impl.PropertyColumnElement] sets `chosen-property` on the slotted `tg-ui-action` children of this element from the column's context (the property name for static columns, the dynamic per-cell binding for dynamic columns).
///
public class FunctionalMultiActionElement implements IRenderable, IImportable {

    private final String widgetName;
    private final String widgetPath;

    private final List<FunctionalActionElement> actionElements = new ArrayList<>();

    /// Creates a multi-action element from a multi-action config, a starting action number, and the action kind.
    /// Inner sub-actions are assigned global indices `numberOfAction + i` and tagged with the supplied kind.
    ///
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

    /// Builds the comma-prefixed JavaScript object literal for every sub-action of this group.
    /// Adds each sub-action's import path into `importPaths` along the way.
    ///
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
