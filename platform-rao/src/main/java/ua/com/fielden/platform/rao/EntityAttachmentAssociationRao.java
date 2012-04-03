package ua.com.fielden.platform.rao;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.attachment.EntityAttachmentAssociation;
import ua.com.fielden.platform.attachment.IAttachmentController2;
import ua.com.fielden.platform.attachment.IEntityAttachmentAssociationController2;
import ua.com.fielden.platform.dao2.SinglePage2;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.fetchAll;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.pagination.IPage2;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * This is a default RAO implementation for managing association between attachments and entities.
 *
 * @author TG Team
 *
 */
@EntityType(EntityAttachmentAssociation.class)
public class EntityAttachmentAssociationRao extends CommonEntityRao<EntityAttachmentAssociation> implements IEntityAttachmentAssociationController2 {

    private final IAttachmentController2 attachmentController;

    @Inject
    public EntityAttachmentAssociationRao(final RestClientUtil restUtil, final IAttachmentController2 attachmentController) {
	super(restUtil);
	this.attachmentController = attachmentController;
    }

    @Override
    public IAttachmentController2 getAttachmentController() {
        return attachmentController;
    }

    @Override
    public IPage2<EntityAttachmentAssociation> findDetails(final AbstractEntity<?> masterEntity, final fetch<EntityAttachmentAssociation> model, final int pageCapacity) {
	final EntityResultQueryModel<EntityAttachmentAssociation> q = select(EntityAttachmentAssociation.class).where().prop("entityId").eq().val(masterEntity.getId()).model();
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
    public EntityAttachmentAssociation save(final EntityAttachmentAssociation entity) {
        return super.save(entity);
    }
}