package ua.com.fielden.platform.sample.domain;

import com.google.inject.Injector;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.report.centre.factory.EntityCentreFactoryBinder;

@EntityType(TgEntityWithPropertyDependency.class)
public class MiTgEntityWithPropertyDependency extends MiWithConfigurationSupport<TgEntityWithPropertyDependency> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "Tg Entity With Property Dependency";
    private static final String description = "<html>" + "<h3>Tg Entity With Property Dependency Centre</h3>"
            + "A facility to query Tg Entity With Property Dependency information.</html>";

    @SuppressWarnings("unchecked")
    public MiTgEntityWithPropertyDependency(final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ITreeMenuItemVisibilityProvider visibilityProvider) {
        super(caption, description, treeMenu, injector.getInstance(EntityCentreFactoryBinder.class), visibilityProvider, MiTgEntityWithPropertyDependency.class, injector.getInstance(IGlobalDomainTreeManager.class));
    }
}