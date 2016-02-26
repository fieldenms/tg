package ua.com.fielden.platform.web.centre.api.resultset.toolbar.impl;

import ua.com.fielden.platform.dom.CssStyles;
import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;
import ua.com.fielden.platform.web.minijs.JsCode;

public class CentreToolbar implements IToolbarConfig {

    protected final InnerTextElement topLevelPlacement = new InnerTextElement("<!-- GENERATED FUNCTIONAL ACTIONS: -->\n<!--@functional_actions-->");
    protected final DomElement configButton = new DomElement("paper-icon-button").clazz("standart-action").
            attr("icon", "icons:settings").attr("on-tap", "_activateSelectionCriteriaView").attr("disabled$", "[[isRunning]]").
            attr("tooltip-text", "Show selection criteria");
    protected final DomElement pagination = new DomContainer().
            add(new DomElement("paper-icon-button").clazz("revers", "standart-action").attr("icon", "hardware:keyboard-tab").
                    attr("on-tap", "firstPage").attr("disabled$", "[[canNotFirst(pageNumber, pageCount, isRunning)]]").
                    attr("tooltip-text", "First page")).
            add(new DomElement("paper-icon-button").clazz("standart-action").attr("icon", "hardware:keyboard-backspace").
                    attr("on-tap", "prevPage").attr("disabled$", "[[canNotPrev(pageNumber, isRunning)]]").attr("tooltip-text", "Previous page")).
            add(new DomElement("span").clazz("standart-action").add(new InnerTextElement("[[currPageFeedback(pageNumberUpdated, pageCountUpdated)]]"))).
            add(new DomElement("paper-icon-button").clazz("revers", "standart-action").attr("icon", "hardware:keyboard-backspace").
                    attr("on-tap", "nextPage").attr("disabled$", "[[canNotNext(pageNumber, pageCount, isRunning)]]").attr("tooltip-text", "Next page")).
            add(new DomElement("paper-icon-button").clazz("standart-action").attr("icon", "hardware:keyboard-tab").
                    attr("on-tap", "lastPage").attr("disabled$", "[[canNotLast(pageNumber, pageCount, isRunning)]]").attr("tooltip-text", "Last page"));
    protected final DomElement refreshButton = new DomElement("paper-icon-button").clazz("standart-action").
            attr("icon", "refresh").attr("on-tap", "currentPage").attr("disabled$", "[[canNotCurrent(pageNumber, pageCount, isRunning)]]").attr("tooltip-text", "Refresh");

    protected DomElement createToolbarElement() {
        return new DomContainer().add(topLevelPlacement, configButton, pagination, refreshButton);
    }

    @Override
    public final DomElement render() {
        return createToolbarElement();
    }

    @Override
    public String importPath() {
        return "polymer/paper-icon-button/paper-icon-button";
    }

    @Override
    public JsCode code(final Class<?> entityType) {
        return new JsCode("");
    }

    @Override
    public CssStyles styles() {
        return new CssStyles();
    }
}
