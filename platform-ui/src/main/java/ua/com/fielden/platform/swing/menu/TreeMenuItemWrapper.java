package ua.com.fielden.platform.swing.menu;

import javax.swing.JPanel;
import javax.swing.tree.MutableTreeNode;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.menu.api.IItemSelector;
import ua.com.fielden.platform.swing.review.DynamicEntityReview;
import ua.com.fielden.platform.swing.review.DynamicEntityReviewWithTabs;

/**
 * {@link TreeMenuItem} that wraps it's parent {@link MiSaveAsConfiguration} node.
 * 
 * @author oleh
 * 
 * @param <T>
 * @param <DAO>
 * @param <R>
 */
public class TreeMenuItemWrapper<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> extends TreeMenuItem<DynamicReportWrapper<T, DAO, R>> implements IItemSelector {

    private static final long serialVersionUID = -5587449424202672352L;

    /**
     * Creates new {@link TreeMenuItemWrapper} instance with specified title and {@link MiSaveAsConfiguration} that must be wrapped.
     * 
     * @param title
     * @param removableDynamicItem
     */
    public TreeMenuItemWrapper(final String title, final boolean isGroup) {
	super(null, title, null, isGroup);
    }

    @Override
    public JPanel getInfoPanel() {
	if (getParent() != null) {
	    return getParent().getInfoPanel();
	}
	return null;
    }

    @Override
    public DynamicReportWrapper<T, DAO, R> getView() {
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
    public TreeMenuItem<DynamicReportWrapper<T, DAO, R>> addItem(final TreeMenuItem<?> item) {
	throw new UnsupportedOperationException("It's impossible to add new child node to this tree menu item wrapper");
    }

    @Override
    public void setParent(final MutableTreeNode newParent) {
	if (newParent != null && !(newParent instanceof MiSaveAsConfiguration)) {
	    throw new IllegalArgumentException("The parent of this tree menu item wrapper must be an instance of MiRemovableDynamicReport class");
	} else {
	    super.setParent(newParent);
	}
    }

    @Override
    public MiSaveAsConfiguration<T, DAO, R> getParent() {
	return (MiSaveAsConfiguration<T, DAO, R>) super.getParent();
    }

    /**
     * Returns wrapped {@link MiSaveAsConfiguration} instance.
     * 
     * @return
     */
    public MiSaveAsConfiguration<T, DAO, R> getWrappedTreeMenuItem() {
	return getParent();
    }

    /**
     * Activates items view.
     * 
     * @param name
     */
    @Override
    public void selectTreeMenuItem(final String name) {
	final DynamicEntityReview<T, DAO, R> view = getView().getView();
	if (view instanceof DynamicEntityReviewWithTabs) {
	    final DynamicEntityReviewWithTabs<T, DAO, R> viewWithTabs = (DynamicEntityReviewWithTabs<T, DAO, R>) view;
	    viewWithTabs.selectTab(name);
	}
    }

    @Override
    public String getTitle() {
	if (getParent() != null) {
	    return getParent().getTitle();
	}
	return super.getTitle();
    }
}
