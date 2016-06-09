package ua.com.fielden.platform.sample.domain;

import com.google.inject.Injector;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.report.centre.factory.EntityCentreFactoryBinder;

@EntityType(User.class)
public class MiUser extends MiWithConfigurationSupport<User> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "User";
    private static final String description = "<html>" + "<h3>User Centre</h3>"
            + "A facility to query User information.</html>";

    @SuppressWarnings("unchecked")
    public MiUser(final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ITreeMenuItemVisibilityProvider visibilityProvider) {
        super(caption, description, treeMenu, injector.getInstance(EntityCentreFactoryBinder.class), visibilityProvider, MiUser.class, injector.getInstance(IGlobalDomainTreeManager.class));
    }
}