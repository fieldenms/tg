package ua.com.fielden.platform.swing.menu;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.report.interfaces.ICentreConfigurationFactory;

import com.google.inject.Injector;

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
	    final Injector injector,//
	    final TreeMenuWithTabs<?> treeMenu,//
	    //Entity centre related parameters
	    final ICentreConfigurationFactory<T> centreFactory,//
	    final ITreeMenuItemVisibilityProvider visibilityProvider,//
	    final Class<? extends MiWithConfigurationSupport<T>> menuItemType) {
	super(new DynamicReportWrapper<T>(caption, description, treeMenu, null, menuItemType, centreFactory, //
		injector.getInstance(IGlobalDomainTreeManager.class), //
		injector.getInstance(EntityFactory.class), //
		injector.getInstance(IEntityMasterManager.class), //
		injector.getInstance(ICriteriaGenerator.class)), visibilityProvider);
	scanForNonPrincipleReports();
    }

    /**
     * Generates children ad hoc reports.
     */
    private void scanForNonPrincipleReports() {
	for(final String centreName : getView().getGlobalDomainTreeManager().entityCentreNames(this.getClass())){
	    final MiSaveAsConfiguration<T> newMenuItem = new MiSaveAsConfiguration<T>(this, centreName);
	    addItem(newMenuItem);
	}
    }
}
