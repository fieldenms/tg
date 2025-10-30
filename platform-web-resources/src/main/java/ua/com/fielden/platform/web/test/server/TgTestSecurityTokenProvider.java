package ua.com.fielden.platform.web.test.server;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.security.tokens.compound_master_menu.UserMaster_OpenMain_MenuItem_CanAccess_Token;
import ua.com.fielden.security.tokens.compound_master_menu.UserMaster_OpenUserAndRoleAssociation_MenuItem_CanAccess_Token;
import ua.com.fielden.security.tokens.open_compound_master.OpenUserMasterAction_CanOpen_Token;

import java.util.Set;

/// Security token provider for the test application.
///
class TgTestSecurityTokenProvider extends SecurityTokenProvider {

    @Inject
    public TgTestSecurityTokenProvider(
            final @Named("tokens.path") String path,
            final @Named("tokens.package") String packageName)
    {
        super(path, packageName,
              Set.of(UserMaster_OpenMain_MenuItem_CanAccess_Token.class,
                     UserMaster_OpenUserAndRoleAssociation_MenuItem_CanAccess_Token.class,
                     OpenUserMasterAction_CanOpen_Token.class),
              Set.of());
    }

}
