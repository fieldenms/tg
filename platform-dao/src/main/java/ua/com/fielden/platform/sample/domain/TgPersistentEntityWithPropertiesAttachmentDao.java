package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgPersistentEntityWithPropertiesAttachment}.
 * 
 * @author TG Team
 *
 */
@EntityType(TgPersistentEntityWithPropertiesAttachment.class)
public class TgPersistentEntityWithPropertiesAttachmentDao extends CommonEntityDao<TgPersistentEntityWithPropertiesAttachment> implements ITgPersistentEntityWithPropertiesAttachment {

    @Inject
    protected TgPersistentEntityWithPropertiesAttachmentDao(final IFilter filter) {
        super(filter);
    }


}