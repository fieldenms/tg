package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.attachment.EntityAttachmentAssociation;
import ua.com.fielden.platform.attachment.IAttachmentController;
import ua.com.fielden.platform.attachment.IEntityAttachmentAssociationController;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;


/**
 * This is a default DAO implementation for managing association between attachments and entities.
 *
 * @author TG Team
 *
 */
@EntityType(EntityAttachmentAssociation.class)
public class EntityAttachmentAssociationDao extends CommonEntityDao<EntityAttachmentAssociation> implements IEntityAttachmentAssociationController {

    private final IAttachmentController attachmentController;

    @Inject
    protected EntityAttachmentAssociationDao(final IFilter filter, final IAttachmentController attachmentController) {
	super(filter);
	this.attachmentController = attachmentController;
    }

    @Override
    public IAttachmentController getAttachmentController() {
        return attachmentController;
    }

    @Override
    public IPage<EntityAttachmentAssociation> findDetails(final AbstractEntity<?> masterEntity, final fetch<EntityAttachmentAssociation> model, final int pageCapacity) {
	final EntityResultQueryModel<EntityAttachmentAssociation> q = select(EntityAttachmentAssociation.class).where().prop("entityId").eq().val(masterEntity).model();
	final OrderingModel ordering = orderBy().prop("attachment.key").asc().model();
	return new SinglePage<EntityAttachmentAssociation>(getAllEntities(from(q).with(ordering).with(fetchAll(EntityAttachmentAssociation.class)).model()));
    }

    @Override
    public EntityAttachmentAssociation saveDetails(final AbstractEntity<?> masterEntity, final EntityAttachmentAssociation detailEntity) {
	return save(detailEntity);
    }

    @Override
    public void deleteDetails(final AbstractEntity<?> masterEntity, final EntityAttachmentAssociation detailEntity) {
	delete(detailEntity);
    }

    @Override
    public void delete(final EntityAttachmentAssociation entity) {
        defaultDelete(entity);
    }
}