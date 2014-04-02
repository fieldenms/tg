package ua.com.fielden.platform.example.dynamiccriteria;

import ua.com.fielden.platform.swing.review.EntityMasterManager;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;

import com.google.inject.Injector;

/**
 * Contains a single static method for creating {@link EntityMasterManager} instance configured for Template Application.
 * 
 * @author TG Team
 */
public class EntityCentreExampleMasterManagerConfig {

    public static EntityMasterManager createEntityMasterFactory(final Injector injector) {
        final EntityMasterManager emm = ((EntityMasterManager) injector.getInstance(IEntityMasterManager.class));//
        //	.addFactory(User.class, injector.getInstance(UserMasterFactory.class))//
        //	.addFactory(UserRole.class, injector.getInstance(UserRoleMasterFactory.class))//
        //	.addFactory(Person.class, injector.getInstance(PersonMasterFactory.class))//
        //	.addFactory(Attachment.class, injector.getInstance(AttachmentMasterFactory.class));
        return emm;
    }

}
