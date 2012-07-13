package ua.com.fielden.platform.swing.menu;

import javax.swing.JPanel;
import javax.swing.tree.MutableTreeNode;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.menu.api.IItemSelector;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationView;
import ua.com.fielden.platform.swing.review.report.events.LoadEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadListener;

/**
 * {@link TreeMenuItem} that wraps it's parent {@link MiSaveAsConfiguration} node.
 *
 * @author oleh
 *
 * @param <T>
 * @param <DAO>
 * @param <R>
 */
public class TreeMenuItemWrapper<T extends AbstractEntity<?>> extends TreeMenuItem<DynamicReportWrapper<T>> implements IItemSelector {

    private static final long serialVersionUID = -5587449424202672352L;

    private final ILoadListener loadListener;

    /**
     * Determines whether associated view should be selected after load or not.
     */
    private boolean selectAfterLoad = false;

    /**
     * Creates new {@link TreeMenuItemWrapper} instance with specified title and {@link MiSaveAsConfiguration} that must be wrapped.
     *
     * @param title
     * @param removableDynamicItem
     */
    public TreeMenuItemWrapper(final String title) {
	super(null, title, null, false);
	this.loadListener = createAnalysisSelectLoadListener();
    }

    @Override
    public JPanel getInfoPanel() {
	if (getParent() != null) {
	    return getParent().getInfoPanel();
	}
	return null;
    }

    @Override
    public DynamicReportWrapper<T> getView() {
	if (getParent() != null) {
	    return getParent().getView();
	}
	return null;
    }

    @Override
    public boolean hasInfoPanel() {
	if (getParent() != null) {
	    return getParent().hasInfoPanel();
	}
	return false;
    }

    @Override
    public TreeMenuItemState getState() {
	if (getParent() != null) {
	    return getParent().getState();
	}
	return TreeMenuItemState.NONE;
    }

    @Override
    public void setState(final TreeMenuItemState state) {
	if (getParent() != null) {
	    getParent().setState(state);
	}
    }

    @Override
    public TreeMenuItem<DynamicReportWrapper<T>> addItem(final TreeMenuItem<?> item) {
	throw new UnsupportedOperationException("It's impossible to add new child node to this tree menu item wrapper");
    }

    @Override
    public void setParent(final MutableTreeNode newParent) {
	if (newParent != null && !(newParent instanceof MiSaveAsConfiguration)) {
	    throw new IllegalArgumentException("The parent of this tree menu item wrapper must be an instance of MiRemovableDynamicReport class");
	} else {
	    final MiSaveAsConfiguration<T> oldParent = getParent();
	    super.setParent(newParent);
	    if(oldParent != newParent){
		if(oldParent != null){
		    oldParent.getView().getCentreConfigurationView().removeLoadListener(loadListener);
		}
		if(newParent != null){
		    getView().getCentreConfigurationView().addLoadListener(loadListener);
		}
	    }
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public MiSaveAsConfiguration<T> getParent() {
	return (MiSaveAsConfiguration<T>) super.getParent();
    }

    /**
     * Returns wrapped {@link MiSaveAsConfiguration} instance.
     *
     * @return
     */
    public MiSaveAsConfiguration<T> getWrappedTreeMenuItem() {
	return getParent();
    }

    @Override
    public void selectTreeMenuItem() {
	final CentreConfigurationView<T, ?> centre = getView().getCentreConfigurationView();
	if(!centre.isLoaded()){
	    selectAfterLoad = true;
	}else if(!selectAfterLoad){
	    selectAnalysisView();
	}
    }

    /**
     * Selects the grid analysis view.
     */
    private void selectAnalysisView(){
	final CentreConfigurationView<T, ?> centre = getView().getCentreConfigurationView();
	if (centre.getModel().getMode() == ReportMode.REPORT) {
	    centre.getPreviousView().selectAnalysis(toString());
	}
    }

    /**
     * Creates the load listener that selects menu item after the centre was loaded.
     *
     * @param centre
     * @return
     */
    private ILoadListener createAnalysisSelectLoadListener() {
	return new ILoadListener() {

	    @Override
	    public void viewWasLoaded(final LoadEvent event) {
		if (selectAfterLoad) {
		    selectAnalysisView();
		    selectAfterLoad = false;
		}
	    }
	};
    }

    @Override
    public String getTitle() {
	if (getParent() != null) {
	    return getParent().getTitle();
	}
	return super.getTitle();
    }
}
