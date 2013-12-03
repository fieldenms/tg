package ua.com.fielden.platform.swing.menu;

import java.util.List;
import java.util.Map.Entry;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.CentreManagerConfigurator;
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
     * @param visibilityProvider
     * @param gdtm TODO
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
	    final CentreManagerConfigurator centreConfigurator, final IGlobalDomainTreeManager gdtm) {
	super(new DynamicReportWrapper<T>(caption, description, treeMenu, null, centreConfigurator, centreBuilder), visibilityProvider);

	// Generates children ad hoc reports.
	for (final Entry<String, List<String>> entry : gdtm.initialCacheOfNonPrincipleItems(centreConfigurator.getMenuItemClass()).entrySet()) {
	    addItem(MiSaveAsConfiguration.<T>createWithProvidedAnalyses(this, entry.getKey(), entry.getValue()));
	}
    }

    /**
     * Creates entity centre configuration with the default {@link CentreManagerConfigurator} instance.
     *
     * @param caption
     * @param description
     * @param treeMenu
     * @param centreBuilder
     * @param visibilityProvider
     * @param menuItemType
     * @param gdtm
     */
    public MiWithConfigurationSupport(//
	    //Menu item related parameters
	    final String caption,//
	    final String description,//
	    final TreeMenuWithTabs<?> treeMenu,//
	    //Entity centre related parameters
	    final IEntityCentreBuilder<T> centreBuilder,//
	    final ITreeMenuItemVisibilityProvider visibilityProvider,//
	    final Class<? extends MiWithConfigurationSupport<T>> menuItemType, final IGlobalDomainTreeManager gdtm) {
	this(caption, description, treeMenu, centreBuilder, visibilityProvider, new CentreManagerConfigurator(menuItemType), gdtm);
    }
}
