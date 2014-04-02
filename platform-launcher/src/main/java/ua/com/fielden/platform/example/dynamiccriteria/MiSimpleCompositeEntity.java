package ua.com.fielden.platform.example.dynamiccriteria;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleCompositeEntity;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.report.centre.factory.EntityCentreFactoryBinder;

import com.google.inject.Injector;

@EntityType(SimpleCompositeEntity.class)
public class MiSimpleCompositeEntity extends MiWithConfigurationSupport<SimpleCompositeEntity> {

    private static final long serialVersionUID = -1454299435907348281L;

    @SuppressWarnings("unchecked")
    public MiSimpleCompositeEntity(final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ITreeMenuItemVisibilityProvider visibilityProvider) {
        super("Simple composite entity", "Simple composite entity description", treeMenu, injector.getInstance(EntityCentreFactoryBinder.class), visibilityProvider, MiSimpleCompositeEntity.class, injector.getInstance(IGlobalDomainTreeManager.class));
    }
}
