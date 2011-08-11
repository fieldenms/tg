package ua.com.fielden.platform.swing.chartscroll;

import java.util.EventListener;

public interface IAutoRangeChangedListener extends EventListener {

    void autoRangeChanged(final AutoRangeChangedEvent event);
}
