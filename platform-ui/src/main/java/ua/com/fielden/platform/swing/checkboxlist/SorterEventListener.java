package ua.com.fielden.platform.swing.checkboxlist;

import java.util.EventListener;

public interface SorterEventListener<T> extends EventListener {

    void valueChanged(SorterChangedEvent<T> e);
}
