package ua.com.fielden.platform.sample.domain;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.report.centre.factory.EntityCentreFactoryBinder;

import com.google.inject.Injector;

@EntityType(TgFetchProviderTestEntity.class)
public class MiTgFetchProviderTestEntity extends MiWithConfigurationSupport<TgFetchProviderTestEntity> {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(MiTgFetchProviderTestEntity.class);

    private static final String caption = "TgFetchProviderTestEntity";
    private static final String description = "<html>" + "<h3>TgFetchProviderTestEntity Centre</h3>"
            + //
            "A facility to query TgFetchProviderTestEntity information.</html>";

    @SuppressWarnings("unchecked")
    public MiTgFetchProviderTestEntity(final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ITreeMenuItemVisibilityProvider visibilityProvider) {
        super(caption, description, treeMenu, injector.getInstance(EntityCentreFactoryBinder.class), visibilityProvider, MiTgFetchProviderTestEntity.class, injector.getInstance(IGlobalDomainTreeManager.class));
    }
}