package ua.com.fielden.platform.dao2;

import ua.com.fielden.platform.attachment.EntityAttachmentAssociation;
import ua.com.fielden.platform.attachment.IAttachmentController2;
import ua.com.fielden.platform.attachment.IEntityAttachmentAssociationController2;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.fetchAll;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.pagination.IPage2;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

import static ua.com.fielden.platform.entity.query.fluent.query.from;
import static ua.com.fielden.platform.entity.query.fluent.query.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.query.select;


/**
 * This is a default DAO implementation for managing association between attachments and entities.
 *
 * @author TG Team
 *
 */
@EntityType(EntityAttachmentAssociation.class)
public class EntityAttachmentAssociationDao2 extends CommonEntityDao2<EntityAttachmentAssociation> implements IEntityAttachmentAssociationController2 {

    private final IAttachmentController2 attachmentController;

    @Inject
    protected EntityAttachmentAssociationDao2(final IFilter filter, final IAttachmentController2 attachmentController) {
	super(filter);
	this.attachmentController = attachmentController;
    }

    @Override
    public IAttachmentController2 getAttachmentController() {
        return attachmentController;
    }

    @Override
    public IPage2<EntityAttachmentAssociation> findDetails(final AbstractEntity<?> masterEntity, final fetch<EntityAttachmentAssociation> model, final int pageCapacity) {
	final EntityResultQueryModel<EntityAttachmentAssociation> q = select(EntityAttachmentAssociation.class).where().prop("entityId").eq().val(masterEntity).model();
	final OrderingModel ordering = orderBy().prop("attachment.key").asc().model();
	return new SinglePage2<EntityAttachmentAssociation>(getEntities(from(q).with(ordering).with(new fetchAll<EntityAttachmentAssociation>(EntityAttachmentAssociation.class)).build()));
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