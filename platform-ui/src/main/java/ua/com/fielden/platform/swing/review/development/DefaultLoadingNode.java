package ua.com.fielden.platform.swing.review.development;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.swing.review.report.events.LoadEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadNotifier;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadingNode;

public class DefaultLoadingNode implements ILoadingNode {

    /**
     * The load notifier with which this loading node is associated.
     */
    private final ILoadNotifier loadNotifier;

    /**
     * Holds the children that must be loaded before this loading node will be loaded.
     */
    private final List<ILoadingNode> children = new ArrayList<>();

    /**
     * Parent of this loading node, that waits until this node will be loaded.
     */
    protected ILoadingNode parent;

    /**
     * Determines whether this node was loaded or not.
     */
    private boolean loaded;

    /**
     * Creates {@link DefaultLoadingNode} without {@link ILoadNotifier}.
     */
    public DefaultLoadingNode(){
	this(null);
    }

    /**
     * Creates {@link DefaultLoadingNode} with specific {@link ILoadNotifier}.
     *
     * @param loadNotifier
     */
    public DefaultLoadingNode(final ILoadNotifier loadNotifier){
	this.loadNotifier = loadNotifier;
	this.parent = null;
	this.loaded = false;
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
	return new ArrayList<>(children);
    }

    /**
     * Adds the specified child to the children list.
     *
     * @param child
     */
    public void addLoadingChild(final ILoadingNode child){
	if(!children.contains(child)){
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
    public boolean removeLoadingChild(final ILoadingNode child){
	if(children.remove(child)){
	    child.setLoadingParent(null);
	    return true;
	}
	return false;
    }

    @Override
    public void tryLoading() {
	if(!loaded && wereChildrenLoaded()){
	    loaded = true;
	    fireLoadEvent(new LoadEvent(this));
	    tryLoadParent();
	}
    }

    @Override
    public boolean isLoaded() {
	return loaded;
    }

    /**
     * Resets the loaded state of this loading node.
     */
    public void reset(){
	loaded = false;
    }

    /**
     * Determines whether children were loaded or not.
     *
     * @return
     */
    public boolean wereChildrenLoaded(){
	for(final ILoadingNode child : children){
	    if(!child.isLoaded()) {
		return false;
	    }
	}
	return true;
    }


    private void fireLoadEvent(final LoadEvent loadEvent){
	if(loadNotifier != null){
	    loadNotifier.fireLoadEvent(loadEvent);
	}
    }

    /**
     * Tries to load parent if it is no null.
     */
    private void tryLoadParent(){
	if(parent != null){
	    parent.tryLoading();
	}
    }
}
