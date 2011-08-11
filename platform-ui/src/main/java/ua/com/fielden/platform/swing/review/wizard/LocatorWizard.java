package ua.com.fielden.platform.swing.review.wizard;

import javax.swing.Action;

import ua.com.fielden.platform.swing.dynamicreportstree.LocatorTree;

/**
 * Wizard for dynamic entity locator.
 *
 * @author TG Team
 *
 */
public class LocatorWizard extends AbstractWizard<LocatorWizardModel, LocatorTree> {
    private static final long serialVersionUID = -6343151315499190679L;

    public LocatorWizard(final LocatorWizardModel wizardModel, final Action buildAction, final Action cancelAction) {
	super(wizardModel, buildAction, cancelAction);
    }

    @Override
    protected LocatorTree createTree() {
	return new LocatorTree(getWizardModel().createTreeModel());
    }
}
