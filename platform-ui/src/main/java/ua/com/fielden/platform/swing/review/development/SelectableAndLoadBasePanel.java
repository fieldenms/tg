package ua.com.fielden.platform.swing.review.development;

import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.swing.review.report.events.LoadEvent;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadNotifier;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadingNode;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectable;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.view.BasePanel;

/**
 * Base class for all views those are interested in selection events.
 * 
 * @author TG Team
 * 
 */
public class SelectableAndLoadBasePanel extends BasePanel implements ISelectable, ILoadNotifier, ILoadingNode {

    private static final long serialVersionUID = 3605564440314097616L;

    /**
     * Holds the children that must be loaded before this loading node will be loaded.
     */
    private final List<ILoadingNode> children = new ArrayList<ILoadingNode>();

    /**
     * Parent of this loading node, that waits until this node will be loaded.
     */
    private ILoadingNode parent;

    /**
     * Determines whether this node was loaded or not.
     */
    private boolean loaded;

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
    public void close() {
        loaded = false;
        super.close();
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
    public void select() {
        fireSelectionEvent(new SelectionEvent(this));
    }

    @Override
    public synchronized void addLoadListener(final ILoadListener listener) {
        listenerList.add(ILoadListener.class, listener);
    }

    @Override
    public synchronized void removeLoadListener(final ILoadListener listener) {
        listenerList.remove(ILoadListener.class, listener);
    }

    @Override
    public String getInfo() {
        return "Default info! Override this in the SelectableAndLoadBasePanel";
    }

    @Override
    public ILoadingNode getLoadingParent() {
        return parent;
    }

    @Override
    public void setLoadingParent(final ILoadingNode parent) {
        this.parent = parent;
    }

    @Override
    public List<ILoadingNode> loadingChildren() {
        return new ArrayList<>();
    }

    @Override
    public void tryLoading() {
        if (!loaded && wereChildrenLoaded()) {
            loaded = true;
            fireLoadEvent(new LoadEvent(this));
            if (parent != null) {
                parent.tryLoading();
            }
        }
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Adds new {@link DefaultLoadingNode} child.
     * 
     * @param child
     */
    public void addLoadingChild(final ILoadingNode child) {
        if (!children.contains(child)) {
            children.add(child);
            child.setLoadingParent(this);
        }
    }

    /**
     * Removes the loading node from the list of children.
     * 
     * @param child
     * @return
     */
    public boolean removeLoadingChild(final ILoadingNode child) {
        if (children.remove(child)) {
            child.setLoadingParent(null);
            return true;
        }
        return false;
    }

    /**
     * Notifies all registered {@link ISelectionEventListener} that this panel model was selected.
     * 
     * @param event
     */
    protected final void fireSelectionEvent(final SelectionEvent event) {
        for (final ISelectionEventListener listener : listenerList.getListeners(ISelectionEventListener.class)) {
            listener.viewWasSelected(event);
        }
    }

    /**
     * Notifies all registered {@link ILoadListener} that this panel model was loaded.
     * 
     * @param loadEvent
     */
    @Override
    public final void fireLoadEvent(final LoadEvent loadEvent) {
        for (final ILoadListener listener : listenerList.getListeners(ILoadListener.class)) {
            listener.viewWasLoaded(loadEvent);
        }
    }

    /**
     * Determines whether children were loaded or not.
     * 
     * @return
     */
    public boolean wereChildrenLoaded() {
        for (final ILoadingNode child : children) {
            if (!child.isLoaded()) {
                return false;
            }
        }
        return true;
    }
}
