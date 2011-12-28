package ua.com.fielden.platform.swing.review.report.interfaces;

import java.util.EventListener;

import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;

public interface ISelectionEventListener extends EventListener {

    void viewWasSelected(SelectionEvent event);

}
