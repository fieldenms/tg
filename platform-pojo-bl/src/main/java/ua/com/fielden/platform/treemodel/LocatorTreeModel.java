package ua.com.fielden.platform.treemodel;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Tree model for locator wizard tree.
 *
 * @author TG Team
 *
 */
public class LocatorTreeModel extends EntityTreeModel {
    private static final long serialVersionUID = -3429054334771648751L;

    // first and second checkbox : no parameters should be used for locator tree.

    public LocatorTreeModel(final Class<? extends AbstractEntity> mainClass, final IPropertyFilter propertyFilter) {
	super(mainClass, propertyFilter, false);
    }
}
