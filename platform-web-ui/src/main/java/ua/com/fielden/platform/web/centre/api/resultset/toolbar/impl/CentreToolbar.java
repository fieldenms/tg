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

/**
 * Configuration for default toolbars available for entity centres and alternative views.
 * This implementation can be overridden to provide adjustments for button placement and contents.
 *
 * @author TG Team
 *
 */
public class CentreToolbar implements IToolbarConfig {

    private static final int DEFAULT_WIDTH_FOR_VIEW_SWITCH = 60;
    private final int switchViewButtonWidth;

    /**
     * Creates standard {@link CentreToolbar}. Standard switch view button width will be 60 (if present).
     *
     * @param switchViewButtonWidth
     */
    public CentreToolbar() {
        this(DEFAULT_WIDTH_FOR_VIEW_SWITCH); //Default width of switch view button
    }

    /**
     * Creates standard {@link CentreToolbar} with custom switch view button width (if present).
     *
     * @param switchViewButtonWidth
     */
    public CentreToolbar(final int switchViewButtonWidth) {
        this.switchViewButtonWidth = switchViewButtonWidth;
    }

    /**
     * Creates standard {@link CentreToolbar} with custom switch view button width (if present).
     *
     * @param switchViewButtonWidth
     */
    public static CentreToolbar withSwitchViewButtonWidth(final int switchViewButtonWidth) {
        return new CentreToolbar(switchViewButtonWidth);
    }

    protected DomElement createToolbarElement() {
        return new DomContainer().add(topLevelPlacement, switchViewPlacement, configButton(), pagination("standart-action"), refreshButton(), helpButton());
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

    public static DomElement helpButton() {
        return new DomElement("paper-icon-button")
                .style("color:#727272")
                .attr("slot", "standart-action")
                .attr("icon", "icons:help-outline")
                .attr("on-mousedown", "_helpMouseDownEventHandler")
                .attr("on-touchstart", "_helpMouseDownEventHandler")
                .attr("on-mouseup", "_helpMouseUpEventHandler")
                .attr("on-touchend", "_helpMouseUpEventHandler")
                .attr("tooltip-text", "Tap to open help in a window or tap with Ctrl/Cmd to open help in a tab.<br>Alt&nbsp+&nbspTap or long touch to edit the help link.");
    }

    public static DomElement selectView(final int viewIndex, final int width) {
        return new DomElement("tg-dropdown-switch")
                .attr("slot", "standart-action")
                .attr("view-index", viewIndex)
                .attr("button-width", width)
                .attr("main-button-tooltip-text", "Choose alternative view")
                .attr("views", "[[resultViews]]");
    }

    public static List<String> configShortcut() {
        return Arrays.asList("ctrl+e");
    }

    public static DomElement pagination(final String slot) {
        return new DomContainer()
                .add(new DomElement("paper-icon-button")
                        .clazz("revers", "standart-action")
                        .attr("slot", slot)
                        .attr("shortcut", "ctrl+up")
                        .attr("icon", "hardware:keyboard-tab")
                        .attr("on-tap", "firstPage")
                        .attr("disabled$", "[[canNotFirst(pageNumber, pageCount, isRunning)]]")
                        .attr("tooltip-text", "First page, Ctrl&nbsp+&nbsp<span style=\"font-size:18px;font-weight:bold\">&#8593</span>"))
                .add(new DomElement("paper-icon-button")
                        .clazz("standart-action")
                        .attr("slot", slot)
                        .attr("shortcut", "ctrl+left")
                        .attr("icon", "hardware:keyboard-backspace")
                        .attr("on-tap", "prevPage")
                        .attr("disabled$", "[[canNotPrev(pageNumber, isRunning)]]")
                        .attr("tooltip-text", "Previous page, Ctrl&nbsp+&nbsp<span style=\"font-size:18px;font-weight:bold\">&#8592</span>"))
                .add(new DomElement("span")
                        .clazz("standart-action pagination-text")
                        .style("white-space:nowrap")
                        .attr("slot", slot)
                        .add(new InnerTextElement("[[currPageFeedback(pageNumberUpdated, pageCountUpdated)]]")))
                .add(new DomElement("paper-icon-button")
                        .clazz("revers", "standart-action")
                        .attr("slot", slot)
                        .attr("shortcut", "ctrl+right")
                        .attr("icon", "hardware:keyboard-backspace")
                        .attr("on-tap", "nextPage")
                        .attr("disabled$", "[[canNotNext(pageNumber, pageCount, isRunning)]]")
                        .attr("tooltip-text", "Next page, Ctrl&nbsp+&nbsp<span style=\"font-size:18px;font-weight:bold\">&#8594</span>"))
                .add(new DomElement("paper-icon-button")
                        .clazz("standart-action")
                        .attr("slot", slot)
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
                .attr("on-tap", "currentPageTap")
                .attr("disabled$", "[[canNotCurrent(pageNumber, pageCount, isRunning)]]")
                .attr("tooltip-text", "Refresh, F5");
    }

    @Override
    public int getSwitchViewButtonWidth() {
        return switchViewButtonWidth;
    }
    
    /**
     * A convenient factory method to create a toolbar that would include top actions, a view switch button, and a refresh action.
     *
     * @return
     */
    public static IToolbarConfig withTopActionsViewSwitchAndRefresh() {
        return withTopActionsViewSwitchAndRefresh(DEFAULT_WIDTH_FOR_VIEW_SWITCH);
    }

    /**
     * The same as {link {@link #withTopActionsViewSwitchAndRefresh()}, but accepts a desired width for the alternative view switching button.
     *
     * @param switchViewButtonWidth
     * @return
     */
    public static IToolbarConfig withTopActionsViewSwitchAndRefresh(final int switchViewButtonWidth) {
        return new CentreToolbar(switchViewButtonWidth) {
            @Override
            protected DomElement createToolbarElement() {
                return new DomContainer().add(topLevelPlacement, switchViewPlacement, refreshButton());
            }
        };
    }

    /**
     * A convenient factory method to create a toolbar that would include top actions, a view switch button, a pagination panel, and a refresh action.
     *
     * @return
     */
    public static IToolbarConfig withTopActionsViewSwitchPeginationAndRefresh() {
        return withTopActionsViewSwitchPeginationAndRefresh(DEFAULT_WIDTH_FOR_VIEW_SWITCH);
    }

    /**
     * The same as {link {@link #withTopActionsViewSwitchPeginationAndRefreshAction()}, but accepts a desired width for the alternative view switching button.
     *
     * @param switchViewButtonWidth
     * @return
     */
    public static IToolbarConfig withTopActionsViewSwitchPeginationAndRefresh(final int switchViewButtonWidth) {
        return new CentreToolbar(switchViewButtonWidth) {
            @Override
            protected DomElement createToolbarElement() {
                return new DomContainer().add(topLevelPlacement, switchViewPlacement, pagination("standart-action"), refreshButton());
            }
        };
    }

    /**
     * A convenient factory method to create a toolbar that would include top actions, a view switch button, a filter button, a pagination panel, and a refresh action.
     *
     * @return
     */
    public static IToolbarConfig withTopActionsViewSwitchFilterPeginationAndRefresh() {
        return withTopActionsViewSwitchFilterPeginationAndRefresh(DEFAULT_WIDTH_FOR_VIEW_SWITCH);
    }

    public static IToolbarConfig withTopActionsViewSwitchFilterPeginationAndRefresh(final int switchViewButtonWidth) {
        return new CentreToolbar(switchViewButtonWidth) {
            @Override
            protected DomElement createToolbarElement() {
                return new DomContainer().add(topLevelPlacement, switchViewPlacement, configButton(), pagination("standart-action"), refreshButton());
            }
        };
    }

}