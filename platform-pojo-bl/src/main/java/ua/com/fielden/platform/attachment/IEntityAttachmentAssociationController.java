package ua.com.fielden.platform.attachment;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IMasterDetailsDao;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A controller for managing DAO related operations with {@link EntityAttachmentAssociation}.
 *
 * @author TG Team
 *
 */
public interface IEntityAttachmentAssociationController extends IEntityDao<EntityAttachmentAssociation>, IMasterDetailsDao<AbstractEntity<?>, EntityAttachmentAssociation> {
    IAttachmentController getAttachmentController();
}
