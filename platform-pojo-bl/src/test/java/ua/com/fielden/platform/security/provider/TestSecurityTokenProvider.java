package ua.com.fielden.platform.security.provider;

import com.google.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.security.tokens.*;
import ua.com.fielden.platform.security.tokens.persistent.TgFuelType_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgFuelType_CanModify_guarded_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgFuelType_CanSaveNew_Token;

import java.util.Set;

@Singleton
final class TestSecurityTokenProvider extends SecurityTokenProvider {

    @Inject
    public TestSecurityTokenProvider(
            final @Named("tokens.path") String path,
            final @Named("tokens.package") String packageName)
    {
        super(path, packageName,
              // Extra tokens
              Set.of(FirstLevelSecurityToken1.class,
                     FirstLevelSecurityToken2.class,
                     SecondLevelSecurityToken1.class,
                     SecondLevelSecurityToken2.class,
                     ThirdLevelSecurityToken1.class,
                     ThirdLevelSecurityToken2.class,
                     TgFuelType_CanModify_guarded_Token.class,
                     TgFuelType_CanDelete_Token.class,
                     TgFuelType_CanSaveNew_Token.class
              ),
              Set.of());
    }

}
