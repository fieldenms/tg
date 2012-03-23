package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.attachment.EntityAttachmentAssociation;
import ua.com.fielden.platform.attachment.IAttachmentController;
import ua.com.fielden.platform.attachment.IEntityAttachmentAssociationController;
import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.dao2.SinglePage2;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.fetchAll;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.pagination.IPage2;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

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
public class EntityAttachmentAssociationDao extends CommonEntityDao2<EntityAttachmentAssociation> implements IEntityAttachmentAssociationController {

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
    public IPage2<EntityAttachmentAssociation> findDetails(final AbstractEntity<?> masterEntity, final fetch<EntityAttachmentAssociation> model, final int pageCapacity) {
	final EntityResultQueryModel<EntityAttachmentAssociation> q = select(EntityAttachmentAssociation.class).where().prop("entityId").eq().val(masterEntity).model();
	final OrderingModel orderBy = orderBy().prop("attachment.key").asc().model();
	return new SinglePage2<EntityAttachmentAssociation>(getAllEntities(from(q).with(new fetchAll<EntityAttachmentAssociation>(EntityAttachmentAssociation.class)).with(orderBy).build()));
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
