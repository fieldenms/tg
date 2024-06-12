package ua.com.fielden.platform.sample.domain;
import java.util.Collection;

import com.google.inject.Inject;

import ua.com.fielden.platform.attachment.AbstractAttachmentDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgCategoryAttachment}.
 *
 * @author TG Team
 */
@EntityType(TgCategoryAttachment.class)
public class TgCategoryAttachmentDao extends AbstractAttachmentDao<TgCategoryAttachment> implements ITgCategoryAttachment {

    @Override
    @SessionRequired
    public TgCategoryAttachment save(final TgCategoryAttachment entity) {
        return super.save(entity);
    }

    @Override
    @SessionRequired
    public int batchDelete(final Collection<Long> entitiesIds) {
        return super.batchDelete(entitiesIds);
    }

}
