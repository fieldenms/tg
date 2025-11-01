package ua.com.fielden.platform.web.centre.api.insertion_points;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.minijs.JsCode;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.join;
import static ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints.ALTERNATIVE_VIEW;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind.INSERTION_POINT;

/**
 * The Insertion point builder class that allows one to render insertion point and generate action object.
 *
 * @author TG Team
 *
 */
public class InsertionPointBuilder implements IRenderable, IExecutable {

    private final InsertionPointConfig insertionPointConfig;
    private final FunctionalActionElement insertionPointActionElement;

    public InsertionPointBuilder(final InsertionPointConfig insertionPointConfig, final int numberOfAction) {
        this.insertionPointConfig = insertionPointConfig;
        this.insertionPointActionElement = new FunctionalActionElement(insertionPointConfig.getInsertionPointAction(), numberOfAction, INSERTION_POINT);
    }

    /**
     * Renders the insertion point DOM.
     */
    @Override
    public DomElement render() {
        final DomElement insertionPointDom = new DomElement("tg-entity-centre-insertion-point")
                .attr("id", "ip" + insertionPointActionElement.numberOfAction)
                .attr("functional-master-tag-name", insertionPointActionElement.generateElementName().toUpperCase())
                .attr("short-desc", insertionPointActionElement.conf().shortDesc.orElse(""))
                .attr("long-desc", insertionPointActionElement.conf().longDesc.orElse(""))
                .attr("icon", insertionPointActionElement.conf().icon.orElse(""))
                .attr("icon-style", insertionPointActionElement.conf().iconStyle.orElse(""))
                .attr("selection-criteria-entity", "[[selectionCriteriaEntity]]")
                .attr("data-change-reason", "[[dataChangeReason]]")
                .attr("retrieved-entities", "{{retrievedEntities}}")
                .attr("all-retrieved-entities", "{{allRetrievedEntities}}")
                .attr("rendering-hints", "{{renderingHints}}")
                .attr("all-rendering-hints", "{{allRenderingHints}}")
                .attr("retrieved-totals", "{{retrievedTotals}}")
                .attr("centre-selection", "[[centreSelection]]")
                .attr("centre-state", "[[currentState]]")
                .attr("column-properties-mapper", "{{columnPropertiesMapper}}")
                .attr("context-retriever", "[[insertionPointContextRetriever]]")
                .attr("custom-shortcuts", join(insertionPointConfig.getToolbar().map(toolbar -> toolbar.getAvailableShortcuts()).orElse(new ArrayList<>()), " "));
        insertionPointConfig.getToolbar().ifPresent(toolbar -> insertionPointDom.add(toolbar.render()));
        if (whereToInsert() == ALTERNATIVE_VIEW) {
            insertionPointDom.attr("alternative-view", true);
            insertionPointDom.attr("slot", "alternative-view-insertion-point");
        } else {
            insertionPointDom.attr("without-resizing", insertionPointConfig.isNoResizing());
        }
        return insertionPointDom;
    }

    /**
     * Generates the action's JS object.
     */
    @Override
    public JsCode code() {
        return new JsCode(insertionPointActionElement.createActionObject());
    }

    /**
     * Renders the insertion point's action DOM
     *
     * @return
     */
    public DomElement renderInsertionPointAction() {
        return insertionPointActionElement.render().clazz("insertion-point-action").attr("hidden", null);
    }

    /**
     * Returns the import paths for this insertion point
     *
     * @return
     */
    public Set<String> importPaths() {
        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        importPaths.add(insertionPointActionElement.importPath());
        return importPaths;
    }

    public Optional<IToolbarConfig> toolbar() {
        return insertionPointConfig.getToolbar();
    }

    public boolean isPreferred() {
        return insertionPointConfig.isPreferred();
    }

    public List<EntityActionConfig> getActions() {
        return insertionPointConfig.getActions();
    }

    /**
     * Determines the place where to insert the insertion point on entity centre.
     *
     * @return
     */
    public InsertionPoints whereToInsert() {
        return insertionPointActionElement.conf().whereToInsertView.get();
    }

    public Optional<String> icon() {
        return insertionPointActionElement.conf().icon;
    }

    public Optional<String> viewTitle() {
        return insertionPointActionElement.conf().shortDesc;
    }
}
