package ua.com.fielden.platform.web.centre.api.resultset.impl;

public enum FunctionalActionKind {

    TOP_LEVEL("topLevelActions"), 
    PRIMARY_RESULT_SET("primaryAction"), 
    SECONDARY_RESULT_SET("secondaryActions"), 
    PROP("propActions"), 
    INSERTION_POINT("insertionPointActions"),
    MENU_ITEM("menuItemActions");

    public final String holderName;

    private FunctionalActionKind(final String holderName) {
        this.holderName = holderName;
    }

}
