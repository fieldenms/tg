package ua.com.fielden.platform.sample.domain;

import org.apache.log4j.Logger;

import com.google.inject.Injector;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.report.centre.factory.EntityCentreFactoryBinder;

@EntityType(TgCollectionalSerialisationParent.class)
public class MiTgCollectionalSerialisationParent extends MiWithConfigurationSupport<TgCollectionalSerialisationParent> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "TgCollectionalSerialisationParent";
    private static final String description = "<html>" + "<h3>TgCollectionalSerialisationParent Centre</h3>"
            + //
            "A facility to query TgCollectionalSerialisationParent information.</html>";

    @SuppressWarnings("unchecked")
    public MiTgCollectionalSerialisationParent(final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ITreeMenuItemVisibilityProvider visibilityProvider) {
        super(caption, description, treeMenu, injector.getInstance(EntityCentreFactoryBinder.class), visibilityProvider, MiTgCollectionalSerialisationParent.class, injector.getInstance(IGlobalDomainTreeManager.class));
    }
}