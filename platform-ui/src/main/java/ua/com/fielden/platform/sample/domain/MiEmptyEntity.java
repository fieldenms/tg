package ua.com.fielden.platform.sample.domain;

import com.google.inject.Injector;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.serialisation.jackson.entities.EmptyEntity;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.report.centre.factory.EntityCentreFactoryBinder;

@EntityType(EmptyEntity.class)
public class MiEmptyEntity extends MiWithConfigurationSupport<EmptyEntity> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "MiEmptyEntity";
    private static final String description = "<html>" + "<h3>EmptyEntity Centre</h3>"
            + "A facility to query EmptyEntity information.</html>";

    @SuppressWarnings("unchecked")
    public MiEmptyEntity(final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ITreeMenuItemVisibilityProvider visibilityProvider) {
        super(caption, description, treeMenu, injector.getInstance(EntityCentreFactoryBinder.class), visibilityProvider, MiEmptyEntity.class, injector.getInstance(IGlobalDomainTreeManager.class));
    }
}