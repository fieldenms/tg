package ua.com.fielden.platform.web.centre.api.resultset.scrolling.impl;

import ua.com.fielden.platform.web.centre.api.resultset.scrolling.IScrollConfig;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.IScrollConfigDone;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.IScrollConfigForLeftPanel;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.IScrollConfigHeader;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.IScrollConfigSecondaryActions;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.IScrollConfigSummary;

public class ScrollConfig implements IScrollConfig, IScrollConfigForLeftPanel {

    private boolean fixedCheckboxes = false;
    private boolean fixedCheckboxesWithPrimaryActions = false;
    private boolean fixedSecondaryActions = false;
    private boolean fixedHeader = false;
    private boolean fixedSummary = false;

    public static IScrollConfigForLeftPanel configScroll() {
        return new ScrollConfig();
    };

    private ScrollConfig() {
    }

    @Override
    public IScrollConfigHeader withFixedSecondaryActions() {
        this.fixedSecondaryActions = true;
        return this;
    }

    @Override
    public IScrollConfigSummary withFixedHeader() {
        this.fixedHeader = true;
        return this;
    }

    @Override
    public IScrollConfigDone withFixedSummary() {
        this.fixedSummary = true;
        return this;
    }

    @Override
    public IScrollConfig done() {
        return this;
    }

    @Override
    public IScrollConfigSecondaryActions withFixedCheckboxes() {
        this.fixedCheckboxes = true;
        return this;
    }

    @Override
    public IScrollConfigSecondaryActions withFixedCheckboxesAndPrimaryActions() {
        this.fixedCheckboxesWithPrimaryActions = true;
        return this;
    }

    @Override
    public boolean areCheckboxesFixed() {
        return fixedCheckboxes;
    }

    @Override
    public boolean areCheckboxesWithPrimaryActionsFixed() {
        return fixedCheckboxesWithPrimaryActions;
    }

    @Override
    public boolean areSecondaryActionsFixed() {
        return fixedSecondaryActions;
    }

    @Override
    public boolean areHeaderFixed() {
        return fixedHeader;
    }

    @Override
    public boolean areSummaryFixed() {
        return fixedSummary;
    }

}
