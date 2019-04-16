package ua.com.fielden.platform.web.centre.api.insertion_points;

import static org.apache.commons.lang.StringUtils.join;
import static ua.com.fielden.platform.web.centre.api.resultset.toolbar.impl.CentreToolbar.pagination;
import static ua.com.fielden.platform.web.centre.api.resultset.toolbar.impl.CentreToolbar.paginationShortcut;

import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.minijs.JsCode;

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
        this.insertionPointActionElement = new FunctionalActionElement(insertionPointConfig.getInsertionPointAction(), numberOfAction, FunctionalActionKind.INSERTION_POINT);
    }
    
    /**
     * Renders the insertion point DOM.
     */
    @Override
    public DomElement render() {
        final DomElement insertionPointDom = new DomElement("tg-entity-centre-insertion-point")
                .attr("id", "ip" + insertionPointActionElement.numberOfAction)
                .attr("short-desc", insertionPointActionElement.conf().shortDesc.orElse(""))
                .attr("long-desc", insertionPointActionElement.conf().longDesc.orElse(""))
                .attr("selection-criteria-entity", "[[selectionCriteriaEntity]]")
                .attr("is-centre-running", "[[_triggerRun]]")
                .attr("retrieved-entities", "{{retrievedEntities}}")
                .attr("retrieved-totals", "{{retrievedTotals}}")
                .attr("centre-selection", "[[centreSelection]]")
                .attr("column-properties-mapper", "{{columnPropertiesMapper}}")
                .attr("context-retriever", "[[insertionPointContextRetriever]]");
        if (insertionPointConfig.hasPaginationButtons()) {
            insertionPointDom.add(pagination("insertion-point-child"));
            insertionPointDom.attr("custom-shortcuts", join(paginationShortcut(), " "));
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
        if (insertionPointConfig.hasPaginationButtons()) {
            importPaths.add("polymer/@polymer/paper-icon-button/paper-icon-button");
        }
        return importPaths;
    }

    /**
     * Determines the place where to insert the insertion point on entity centre.
     * 
     * @return
     */
    public InsertionPoints whereToInsert() {
        return insertionPointActionElement.entityActionConfig.whereToInsertView.get();
    }
}
