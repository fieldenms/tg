package ua.com.fielden.platform.swing.menu.filter;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * tree model for {@link JTree} filtering
 * 
 * @author oleh
 * @author 01es
 */
public class FilterableTreeModel extends AbstractFilterableTreeModel {
    private static final long serialVersionUID = 2146620575812602085L;

    /**
     * Instantiates filterable tree model wrapper where filters are chained with <code>AND</code> condition.
     * 
     * @param model
     *            -- tree model to wrap.
     */
    public FilterableTreeModel(final DefaultTreeModel model) {
	this(model, true);
    }

    /**
     * Instantiates filterable tree model wrapper.
     * 
     * @param model
     *            -- tree model to wrap.
     * @param andMode
     *            -- specifies whether filters should be chained with <code>AND</code> condition
     */
    public FilterableTreeModel(final DefaultTreeModel model, final boolean andMode) {
	super(model, andMode);
    }

    @Override
    public DefaultTreeModel getOriginModel() {
	return (DefaultTreeModel) super.getOriginModel();
    }

    @Override
    public void reload() {
	getOriginModel().reload();
    }

    @Override
    public void valueForPathChanged(final TreePath path, final Object newValue) {
	// super.valueForPathChanged(path, newValue);
    }

}
