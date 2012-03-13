package ua.com.fielden.platform.attachment;

import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.dao2.IMasterDetailsDao2;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A controller for managing DAO related operations with {@link EntityAttachmentAssociation}.
 *
 * @author TG Team
 *
 */
public interface IEntityAttachmentAssociationController2 extends IEntityDao2<EntityAttachmentAssociation>, IMasterDetailsDao2<AbstractEntity<?>, EntityAttachmentAssociation> {
    IAttachmentController2 getAttachmentController();
}
