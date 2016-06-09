package ua.com.fielden.platform.sample.domain;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.report.centre.factory.EntityCentreFactoryBinder;

import com.google.inject.Injector;

@EntityType(TgDeletionTestEntity.class)
public class MiDeletionTestEntity extends MiWithConfigurationSupport<TgDeletionTestEntity> {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(TgDeletionTestEntity.class);

    private static final String caption = "Entity for deletion test case";
    private static final String description = "<html>" + "<h3>Entity for deletion test case</h3>"
            + //
            "A facility to query Entity for deletion test case information.</html>";

    @SuppressWarnings("unchecked")
    public MiDeletionTestEntity(final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ITreeMenuItemVisibilityProvider visibilityProvider) {
        super(caption, description, treeMenu, injector.getInstance(EntityCentreFactoryBinder.class), visibilityProvider, MiDeletionTestEntity.class, injector.getInstance(IGlobalDomainTreeManager.class));
    }
}
