package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link IAttachmentPreviewEntityAction}.
 *
 * @author TG Team
 *
 */
@EntityType(EntityEditAction.class)
public class AttachmentPreviewEntityActionDao extends CommonEntityDao<AttachmentPreviewEntityAction> implements IAttachmentPreviewEntityAction {

    @Inject
    protected AttachmentPreviewEntityActionDao(final IFilter filter) {
        super(filter);
    }

}
