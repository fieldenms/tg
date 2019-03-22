package ua.com.fielden.platform.web.centre.api.resultset.toolbar.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ua.com.fielden.platform.dom.CssElement;
import ua.com.fielden.platform.dom.CssStyles;
import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;
import ua.com.fielden.platform.web.minijs.JsCode;

public class CentreToolbar implements IToolbarConfig {

    protected final InnerTextElement topLevelPlacement = new InnerTextElement("<!-- GENERATED FUNCTIONAL ACTIONS: -->\n<!--@functional_actions-->");

    protected DomElement createToolbarElement() {
        return new DomContainer().add(topLevelPlacement, configButton(), pagination(), refreshButton());
    }

    @Override
    public final DomElement render() {
        return createToolbarElement();
    }

    @Override
    public String importPath() {
        return "polymer/@polymer/paper-icon-button/paper-icon-button";
    }

    @Override
    public JsCode code(final Class<?> entityType) {
        return new JsCode("");
    }

    @Override
    public CssStyles styles() {
        return new CssStyles()
                .add(new CssElement("paper-icon-button.orange").setStyle("color", "var(--paper-orange-500)").setStyle("border-color", "var(--paper-orange-500)"));
    }

    @Override
    public List<String> getAvailableShortcuts() {
        return Stream.of(configShortcut(), paginationShortcut(), refreshShortcut()).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static DomElement configButton() {
        return new DomElement("paper-icon-button")
                .attr("slot", "standart-action")
                .attr("shortcut", "ctrl+e")
                .attr("class$", "[[computeConfigButtonClasses(staleCriteriaMessage)]]")
                .attr("icon", "icons:filter-list")
                .attr("on-tap", "_activateSelectionCriteriaView")
                .attr("disabled$", "[[isRunning]]")
                .attr("hidden$", "[[isSelectionCriteriaEmpty]]")
                .attr("tooltip-text$", "[[computeConfigButtonTooltip(staleCriteriaMessage)]]");
    }

    public static List<String> configShortcut() {
        return Arrays.asList("ctrl+e");
    }

    public static DomElement pagination() {
        return new DomContainer()
                .add(new DomElement("paper-icon-button")
                        .clazz("revers", "standart-action")
                        .attr("slot", "standart-action")
                        .attr("shortcut", "ctrl+up")
                        .attr("icon", "hardware:keyboard-tab")
                        .attr("on-tap", "firstPage")
                        .attr("disabled$", "[[canNotFirst(pageNumber, pageCount, isRunning)]]")
                        .attr("tooltip-text", "First page, Ctrl&nbsp+&nbsp<span style=\"font-size:18px;font-weight:bold\">&#8593</span>"))
                .add(new DomElement("paper-icon-button")
                        .clazz("standart-action")
                        .attr("slot", "standart-action")
                        .attr("shortcut", "ctrl+left")
                        .attr("icon", "hardware:keyboard-backspace")
                        .attr("on-tap", "prevPage")
                        .attr("disabled$", "[[canNotPrev(pageNumber, isRunning)]]")
                        .attr("tooltip-text", "Previous page, Ctrl&nbsp+&nbsp<span style=\"font-size:18px;font-weight:bold\">&#8592</span>"))
                .add(new DomElement("span")
                        .clazz("standart-action")
                        .attr("slot", "standart-action")
                        .add(new InnerTextElement("[[currPageFeedback(pageNumberUpdated, pageCountUpdated)]]")))
                .add(new DomElement("paper-icon-button")
                        .clazz("revers", "standart-action")
                        .attr("slot", "standart-action")
                        .attr("shortcut", "ctrl+right")
                        .attr("icon", "hardware:keyboard-backspace")
                        .attr("on-tap", "nextPage")
                        .attr("disabled$", "[[canNotNext(pageNumber, pageCount, isRunning)]]")
                        .attr("tooltip-text", "Next page, Ctrl&nbsp+&nbsp<span style=\"font-size:18px;font-weight:bold\">&#8594</span>"))
                .add(new DomElement("paper-icon-button")
                        .clazz("standart-action")
                        .attr("slot", "standart-action")
                        .attr("shortcut", "ctrl+down")
                        .attr("icon", "hardware:keyboard-tab")
                        .attr("on-tap", "lastPage")
                        .attr("disabled$", "[[canNotLast(pageNumber, pageCount, isRunning)]]")
                        .attr("tooltip-text", "Last page, Ctrl&nbsp+&nbsp<span style=\"font-size:18px;font-weight:bold\">&#8595</span>"));
    }

    public static List<String> paginationShortcut() {
        return Arrays.asList( "ctrl+down", "ctrl+left", "ctrl+right", "ctrl+up");
    }

    public static List<String> refreshShortcut () {
        return Arrays.asList( "f5");
    }

    public static DomElement refreshButton() {
        return new DomElement("paper-icon-button")
                .clazz("standart-action")
                .attr("slot", "standart-action")
                .attr("shortcut", "f5")
                .attr("icon", "refresh")
                .attr("on-tap", "currentPage")
                .attr("disabled$", "[[canNotCurrent(pageNumber, pageCount, isRunning)]]")
                .attr("tooltip-text", "Refresh, F5");
    }
}
