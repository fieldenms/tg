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
    private int numOfFixedProps = 0;

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
        this.fixedCheckboxes = true;
        this.fixedCheckboxesWithPrimaryActions = true;
        return this;
    }

    @Override
    public boolean isCheckboxesFixed() {
        return fixedCheckboxes;
    }

    @Override
    public boolean isCheckboxesWithPrimaryActionsFixed() {
        return fixedCheckboxesWithPrimaryActions;
    }

    @Override
    public boolean isSecondaryActionsFixed() {
        return fixedSecondaryActions;
    }

    @Override
    public boolean isHeaderFixed() {
        return fixedHeader;
    }

    @Override
    public boolean isSummaryFixed() {
        return fixedSummary;
    }

    @Override
    public IScrollConfigSecondaryActions withFixedCheckboxesPrimaryActionsAndFirstProps(final int numberOfProps) {
        this.fixedCheckboxes = true;
        this.fixedCheckboxesWithPrimaryActions = true;
        this.numOfFixedProps = numberOfProps;
        return this;
    }

    @Override
    public int getNumberOfFixedColumns() {
        return numOfFixedProps;
    }

}
