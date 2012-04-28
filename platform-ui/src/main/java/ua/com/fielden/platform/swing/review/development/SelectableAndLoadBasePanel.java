package ua.com.fielden.platform.swing.review.development;

import java.awt.LayoutManager;

import ua.com.fielden.platform.swing.review.report.events.LoadEvent;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadNotifier;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectable;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.view.BasePanel;

/**
 * Base class for all views those are interested in selection events.
 * 
 * @author TG Team
 *
 */
public class SelectableAndLoadBasePanel extends BasePanel implements ISelectable, ILoadNotifier {

    private static final long serialVersionUID = 3605564440314097616L;

    /**
     * Default constructor. For convenience.
     */
    public SelectableAndLoadBasePanel() {
    }

    /**
     * Initiates this {@link SelectableAndLoadBasePanel} with {@link LayoutManager}
     * 
     * @param layoutManager
     */
    public SelectableAndLoadBasePanel(final LayoutManager layoutManager) {
	super(layoutManager);
    }

    @Override
    public synchronized void addSelectionEventListener(final ISelectionEventListener l) {
	listenerList.add(ISelectionEventListener.class, l);
    }

    @Override
    public synchronized void removeSelectionEventListener(final ISelectionEventListener l) {
	listenerList.remove(ISelectionEventListener.class, l);
    }

    /**
     * Selects this {@link SelectableAndLoadBasePanel} and fires {@link SelectionEvent}.
     */
    public void select(){
	fireSelectionEvent(new SelectionEvent(this));
    }

    /**
     * Notifies all registered {@link ISelectionEventListener} that this panel model was selected.
     *
     * @param event
     */
    protected final void fireSelectionEvent(final SelectionEvent event){
	for(final ISelectionEventListener listener : listenerList.getListeners(ISelectionEventListener.class)){
	    listener.viewWasSelected(event);
	}
    }

    @Override
    public synchronized void addLoadListener(final ILoadListener listener) {
	listenerList.add(ILoadListener.class, listener);
    }

    @Override
    public synchronized void removeLoadListener(final ILoadListener listener) {
	listenerList.remove(ILoadListener.class, listener);
    }

    /**
     * Notifies all registered {@link ILoadListener} that this panel model was loaded.
     * 
     * @param loadEvent
     */
    protected final void fireLoadEvent(final LoadEvent loadEvent){
	for(final ILoadListener listener : listenerList.getListeners(ILoadListener.class)){
	    listener.viewWasLoaded(loadEvent);
	}
    }

    @Override
    public String getInfo() {
	return "Default info! Override this in the SelectableAndLoadBasePanel";
    }

}
