package ua.com.fielden.platform.swing.review.development;

import java.awt.LayoutManager;

import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectable;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.view.BasePanel;

/**
 * Base class for all views those are interested in selection events.
 * 
 * @author TG Team
 *
 */
public abstract class SelectableBasePanel extends BasePanel implements ISelectable {

    private static final long serialVersionUID = 3605564440314097616L;

    /**
     * Default constructor. For convenience.
     */
    public SelectableBasePanel() {
    }

    /**
     * Initiates this {@link SelectableBasePanel} with {@link LayoutManager}
     * 
     * @param layoutManager
     */
    public SelectableBasePanel(final LayoutManager layoutManager) {
	super(layoutManager);
    }

    @Override
    public void addSelectionEventListener(final ISelectionEventListener l) {
	listenerList.add(ISelectionEventListener.class, l);
    }

    @Override
    public void removeSelectionEventListener(final ISelectionEventListener l) {
	listenerList.remove(ISelectionEventListener.class, l);
    }

    /**
     * Selects this {@link SelectableBasePanel} and fires {@link SelectionEvent}.
     */
    public void select(){
	fireSelectionEvent(new SelectionEvent(this));
    }

    /**
     * Notifies all registered {@link ISelectionEventListener} that this configuration model was selected.
     *
     * @param event
     */
    protected final void fireSelectionEvent(final SelectionEvent event){
	for(final ISelectionEventListener listener : listenerList.getListeners(ISelectionEventListener.class)){
	    listener.viewWasSelected(event);
	}
    }

}
