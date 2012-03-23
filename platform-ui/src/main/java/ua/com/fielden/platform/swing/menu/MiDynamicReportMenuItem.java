package ua.com.fielden.platform.swing.menu;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;

import com.google.inject.Injector;

/**
 * A convenient abstraction for a menu item for entity centres.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <DAO>
 * @param <R>
 */
public abstract class MiDynamicReportMenuItem<T extends AbstractEntity> extends MiWithConfigurationSupport<T> {

    private static final long serialVersionUID = 8838631121425630548L;

    public MiDynamicReportMenuItem(//
	    final String caption,//
	    final String description,//
	    final TreeMenuWithTabs<?> treeMenu,//
	    final Injector injector,//
	    final ITreeMenuItemVisibilityProvider visibilityProvider,//
	    final Class<T> entityType,//
	    final String name//
	    ) {
	super(caption, description, treeMenu,//
		visibilityProvider, entityType, name,//
		injector.getInstance(IGlobalDomainTreeManager.class),//
		injector.getInstance(EntityFactory.class),//
		injector.getInstance(IEntityMasterManager.class),//
		injector.getInstance(ICriteriaGenerator.class));
    }
}
