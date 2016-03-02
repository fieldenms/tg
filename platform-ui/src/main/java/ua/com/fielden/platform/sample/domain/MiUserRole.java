package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.report.centre.factory.EntityCentreFactoryBinder;

import com.google.inject.Injector;

/**
 * Main menu item representing user role centre used for querying, editing and creation of new {@link UserRole}'s.
 * 
 * @author TG Team
 * 
 */
@EntityType(UserRole.class)
public class MiUserRole extends MiWithConfigurationSupport<UserRole> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "User Roles";
    private static final String description = "<html>" + "<h3>User Role Centre</h3>" + //
            "A facility to query user roles based on a number of different criteria. " + //
            "Supports opening of retrieved user roles for editing and creation of new user roles." + "</html>";

    @SuppressWarnings("unchecked")
    public MiUserRole(final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ITreeMenuItemVisibilityProvider visibilityProvider) {
        super(caption, description, treeMenu, injector.getInstance(EntityCentreFactoryBinder.class), visibilityProvider, MiUserRole.class, injector.getInstance(IGlobalDomainTreeManager.class));
    }
}
