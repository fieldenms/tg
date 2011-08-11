package ua.com.fielden.platform.swing.review.wizard;

import javax.swing.Action;

import ua.com.fielden.platform.swing.dynamicreportstree.CriteriaTree;
import ua.com.fielden.platform.treemodel.CriteriaTreeModel;

/**
 * Wizard for dynamic entity query criteria.
 *
 * @author TG Team
 *
 */
public class CriteriaWizard extends AbstractWizard<CriteriaWizardModel, CriteriaTree> {
    private static final long serialVersionUID = -6343151315499190679L;

    public CriteriaWizard(final CriteriaWizardModel wizardModel, final Action buildAction, final Action cancelAction) {
	super(wizardModel, buildAction, cancelAction);
    }

    @Override
    protected CriteriaTree createTree() {
	return new CriteriaTree((CriteriaTreeModel)getWizardModel().getTreeModel());
    }
}
