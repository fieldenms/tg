package ua.com.fielden.platform.web.centre.api.insertion_points;

import static org.apache.commons.lang.StringUtils.join;
import static ua.com.fielden.platform.web.centre.api.resultset.toolbar.impl.CentreToolbar.pagination;
import static ua.com.fielden.platform.web.centre.api.resultset.toolbar.impl.CentreToolbar.paginationShortcut;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.impl.CentreToolbar;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.minijs.JsCode;

public class InsertionPointBuilder implements IRenderable, IExecutable {

    private final InsertionPointConfig insertionPointConfig;
    private final FunctionalActionElement insertionPointActionElement;

    public InsertionPointBuilder(final InsertionPointConfig insertionPointConfig, int numberOfAction) {
        this.insertionPointConfig = insertionPointConfig;
        this.insertionPointActionElement = new FunctionalActionElement(insertionPointConfig.getInsertionPointAction(), numberOfAction, FunctionalActionKind.INSERTION_POINT);
    }
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
                .attr("column-properties-mapper", "{{columnPropertiesMapper}}");
        if (insertionPointConfig.hasPaginationButtons()) {
            insertionPointDom.add(pagination());
            insertionPointDom.attr("custom-shortcuts", join(paginationShortcut(), " "));
        }
        return insertionPointDom;
    }
    @Override
    public JsCode code() {
        return new JsCode(insertionPointActionElement.createActionObject());
    }

    public DomElement renderInsertionPointAction() {
        return insertionPointActionElement.render().clazz("insertion-point-action").attr("hidden", null);
    }

    public String importPath() {
        return insertionPointActionElement.importPath();
    }

    public InsertionPoints whereToInsert() {
        return insertionPointActionElement.entityActionConfig.whereToInsertView.get();
    }
}
