package ua.com.fielden.platform.swing.menu;

import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.centre.factory.IEntityCentreBuilder;

/**
 * The base class for implementing menu items, which require configuration support. Mainly used for representing entity center related menu items.
 *
 * @author TG Team
 *
 */
public class MiWithConfigurationSupport<T extends AbstractEntity<?>> extends MiWithVisibilityProvider<DynamicReportWrapper<T>> {

    private static final long serialVersionUID = -4608369671314218118L;

    /**
     * Creates new {@link MiWithConfigurationSupport} instance and generates all his children reports. Unlike parent report, Children reports can be remove.
     *
     * @param visibilityProvider
     * @param entityType
     * @param gdtm
     * @param entityFactory
     * @param criteriaGenerator
     */
    public MiWithConfigurationSupport(//
	    //Menu item related parameters
	    final String caption,//
	    final String description,//
	    final TreeMenuWithTabs<?> treeMenu,//
	    //Entity centre related parameters
	    final IEntityCentreBuilder<T> centreBuilder,//
	    final ITreeMenuItemVisibilityProvider visibilityProvider,//
	    final Class<? extends MiWithConfigurationSupport<T>> menuItemType) {
	super(new DynamicReportWrapper<T>(caption, description, treeMenu, null, menuItemType, centreBuilder), visibilityProvider);
	/* TODO need to optimise!!! */ scanForNonPrincipleReports();
    }

    /**
     * Generates children ad hoc reports.
     */
    private void scanForNonPrincipleReports() {
	final Set<String> names = new HashSet<String>(getView().getNonPrincipleEntityCentreNames());
	names.remove(null); // remove principle centre key (null), which is returned in case when principle entity centre is persisted
	for(final String centreName : names){
	    final MiSaveAsConfiguration<T> newMenuItem = new MiSaveAsConfiguration<T>(this, centreName);
	    addItem(newMenuItem);
	}
    }
}
