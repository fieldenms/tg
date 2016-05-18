package ua.com.fielden.platform.sample.domain;

import com.google.inject.Injector;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.report.centre.factory.EntityCentreFactoryBinder;

@EntityType(TgPersistentEntityWithProperties.class)
public class MiEntityCentreNotGenerated extends MiWithConfigurationSupport<TgPersistentEntityWithProperties> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "MiEntityCentreNotGenerated";
    private static final String description = "<html>" + "<h3>Tg Persistent Entity With Properties Centre (not generated)</h3>"
            + "A facility to query Tg Persistent Entity With Properties information.</html>";

    @SuppressWarnings("unchecked")
    public MiEntityCentreNotGenerated(final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ITreeMenuItemVisibilityProvider visibilityProvider) {
        super(caption, description, treeMenu, injector.getInstance(EntityCentreFactoryBinder.class), visibilityProvider, MiEntityCentreNotGenerated.class, injector.getInstance(IGlobalDomainTreeManager.class));
    }
}