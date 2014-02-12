package ua.com.fielden.platform.javafx.dashboard2;

import ua.com.fielden.platform.dashboard.IDashboardItemResult;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;

/** An interface for dashboard item UI. */
public interface IDashboardItemUi<RESULT extends IDashboardItemResult> {

    /** Returns the upper component which holds all dashboard UI.*/
    BlockingIndefiniteProgressLayer upperComponent();

    void setUpperComponent(final BlockingIndefiniteProgressLayer upperComponent);

    void update(final RESULT result);
}
