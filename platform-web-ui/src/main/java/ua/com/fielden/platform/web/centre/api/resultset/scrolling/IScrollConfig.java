package ua.com.fielden.platform.web.centre.api.resultset.scrolling;



public interface IScrollConfig {

    boolean isCheckboxesFixed();

    boolean isCheckboxesWithPrimaryActionsFixed();

    boolean isSecondaryActionsFixed();

    boolean isHeaderFixed();

    boolean isSummaryFixed();

    int getNumberOfFixedColumns();
}
