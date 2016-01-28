package ua.com.fielden.platform.web.centre.api.resultset.toolbar.impl;

import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;

public class CentreToolbar implements IToolbarConfig {

    private final DomElement newButton = new DomElement("tg-page-action").clazz("entity-specific-action").attr("tabIndex", "-1").
            attr("icon", "add-circle-outline").attr("action", "[[_newAction]]").attr("short-desc", "Create new entity");
    private final DomElement editButton = new DomElement("tg-page-action").clazz("entity-specific-action").attr("tabIndex", "-1").
            attr("icon", "editor:mode-edit").attr("action", "[[_editAction]]").attr("short-desc", "Edit selected entity");
    private final DomElement deleteButton = new DomElement("tg-page-action").clazz("entity-specific-action").attr("tabIndex", "-1").
            attr("icon", "remove-circle-outline").attr("action", "[[_deleteAction]]").attr("short-desc", "Delete selected entities");
    private final InnerTextElement topLevelPlacement = new InnerTextElement("<!-- GENERATED FUNCTIONAL ACTIONS: -->\n<!--@functional_actions-->");
    private final DomElement configButton = new DomElement("paper-icon-button").clazz("standart-action").
            attr("icon", "icons:settings").attr("on-tap", "_activateSelectionCriteriaView").attr("disabled$", "[[isRunning]]");
    private final DomElement pagination = new DomContainer().
            add(new DomElement("paper-icon-button").clazz("revers", "standart-action").attr("icon", "hardware:keyboard-tab").
                    attr("on-tap", "firstPage").attr("disabled$", "[[canNotFirst(pageNumber, pageCount, isRunning)]]")).
            add(new DomElement("paper-icon-button").clazz("standart-action").attr("icon", "hardware:keyboard-backspace").
                    attr("on-tap", "prevPage").attr("disabled$", "[[canNotPrev(pageNumber, isRunning)]]")).
            add(new DomElement("span").clazz("standart-action").add(new InnerTextElement("[[currPageFeedback(pageNumberUpdated, pageCountUpdated)]]"))).
            add(new DomElement("paper-icon-button").clazz("revers", "standart-action").attr("icon", "hardware:keyboard-backspace").
                    attr("on-tap", "nextPage").attr("disabled$", "[[canNotNext(pageNumber, pageCount, isRunning)]]")).
            add(new DomElement("paper-icon-button").clazz("standart-action").attr("icon", "hardware:keyboard-tab").
                    attr("on-tap", "lastPage").attr("disabled$", "[[canNotLast(pageNumber, pageCount, isRunning)]]"));
    private final DomElement refreshButton = new DomElement("paper-icon-button").clazz("standart-action").
            attr("icon", "refresh").attr("on-tap", "currentPage").attr("disabled$", "[[canNotCurrent(pageNumber, pageCount, isRunning)]]");
    private final DomElement toolbarElement = new DomContainer().add(newButton, editButton, deleteButton, topLevelPlacement, configButton, pagination, refreshButton);

    @Override
    public DomElement render() {
        return toolbarElement;
    }

}
