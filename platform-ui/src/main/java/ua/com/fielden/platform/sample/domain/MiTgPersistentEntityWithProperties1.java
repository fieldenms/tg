package ua.com.fielden.platform.sample.domain;

import com.google.inject.Injector;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.report.centre.factory.EntityCentreFactoryBinder;

@EntityType(TgPersistentEntityWithProperties.class)
public class MiTgPersistentEntityWithProperties1 extends MiWithConfigurationSupport<TgPersistentEntityWithProperties> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "Tg Persistent Entity With Properties 1";
    private static final String description = "<html>" + "<h3>Tg Persistent Entity With Properties Centre</h3>"
            + "A facility to query Tg Persistent Entity With Properties information.</html>";

    @SuppressWarnings("unchecked")
    public MiTgPersistentEntityWithProperties1(final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ITreeMenuItemVisibilityProvider visibilityProvider) {
        super(caption, description, treeMenu, injector.getInstance(EntityCentreFactoryBinder.class), visibilityProvider, MiTgPersistentEntityWithProperties1.class, injector.getInstance(IGlobalDomainTreeManager.class));
    }
}