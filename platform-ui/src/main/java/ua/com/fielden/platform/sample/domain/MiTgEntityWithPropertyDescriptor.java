package ua.com.fielden.platform.sample.domain;

import com.google.inject.Injector;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.report.centre.factory.EntityCentreFactoryBinder;

@EntityType(TgEntityWithPropertyDescriptor.class)
public class MiTgEntityWithPropertyDescriptor extends MiWithConfigurationSupport<TgEntityWithPropertyDescriptor> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "Tg Entity With Property Descriptor";
    private static final String description = "<html>" + "<h3>Tg Entity With Property Descriptor Centre</h3>"
            + "A facility to query Tg Entity With Property Descriptor information.</html>";

    @SuppressWarnings("unchecked")
    public MiTgEntityWithPropertyDescriptor(final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ITreeMenuItemVisibilityProvider visibilityProvider) {
        super(caption, description, treeMenu, injector.getInstance(EntityCentreFactoryBinder.class), visibilityProvider, MiTgEntityWithPropertyDescriptor.class, injector.getInstance(IGlobalDomainTreeManager.class));
    }
}