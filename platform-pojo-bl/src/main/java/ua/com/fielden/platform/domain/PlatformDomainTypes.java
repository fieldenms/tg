package ua.com.fielden.platform.domain;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.EntityAttachmentAssociation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.EntityManipulationAction;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.keygen.KeyNumber;
import ua.com.fielden.platform.migration.MigrationError;
import ua.com.fielden.platform.migration.MigrationHistory;
import ua.com.fielden.platform.migration.MigrationRun;
import ua.com.fielden.platform.sample.domain.MasterInDialogInvocationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.MasterInvocationFunctionalEntity;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.ui.config.EntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityLocatorConfig;
import ua.com.fielden.platform.ui.config.EntityMasterConfig;
import ua.com.fielden.platform.ui.config.MainMenu;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemInvisibility;

public class PlatformDomainTypes {
    public static final List<Class<? extends AbstractEntity<?>>> types = new ArrayList<Class<? extends AbstractEntity<?>>>();

    static {
        types.add(MainMenuItem.class);
        types.add(MainMenuItemInvisibility.class);
        types.add(MainMenu.class);
        types.add(User.class);
        types.add(UserSession.class);
        types.add(UserRole.class);
        types.add(UserAndRoleAssociation.class);
        types.add(SecurityRoleAssociation.class);
        types.add(EntityCentreConfig.class);
        types.add(EntityCentreAnalysisConfig.class);
        types.add(EntityMasterConfig.class);
        types.add(EntityLocatorConfig.class);
        types.add(Attachment.class);
        types.add(EntityAttachmentAssociation.class);
        types.add(KeyNumber.class);
        types.add(MigrationRun.class);
        types.add(MigrationHistory.class);
        types.add(MigrationError.class);
        types.add(CentreContextHolder.class);
        types.add(SavingInfoHolder.class);
        types.add(MasterInvocationFunctionalEntity.class);
        types.add(MasterInDialogInvocationFunctionalEntity.class);
        types.add(EntityManipulationAction.class);
    }
}
