package ua.com.fielden.platform.rao;

import ua.com.fielden.platform.attachment.EntityAttachmentAssociation;
import ua.com.fielden.platform.attachment.IAttachmentController;
import ua.com.fielden.platform.attachment.IEntityAttachmentAssociationController;
import ua.com.fielden.platform.dao.SinglePage;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.fetchAll;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

import static ua.com.fielden.platform.equery.equery.select;

/**
 * This is a default RAO implementation for managing association between attachments and entities.
 *
 * @author TG Team
 *
 */
@EntityType(EntityAttachmentAssociation.class)
public class EntityAttachmentAssociationRao extends CommonEntityRao<EntityAttachmentAssociation> implements IEntityAttachmentAssociationController {

    private final IAttachmentController attachmentController;

    @Inject
    public EntityAttachmentAssociationRao(final RestClientUtil restUtil, final IAttachmentController attachmentController) {
	super(restUtil);
	this.attachmentController = attachmentController;
    }

    @Override
    public IAttachmentController getAttachmentController() {
        return attachmentController;
    }

    @Override
    public IPage<EntityAttachmentAssociation> findDetails(final AbstractEntity<?> masterEntity, final fetch<EntityAttachmentAssociation> model, final int pageCapacity) {
	final IQueryOrderedModel<EntityAttachmentAssociation> q = select(EntityAttachmentAssociation.class).where().prop("entityId").eq().val(masterEntity.getId()).orderBy("attachment.key").model();
	return new SinglePage<EntityAttachmentAssociation>(getEntities(q, new fetchAll<EntityAttachmentAssociation>(EntityAttachmentAssociation.class)));
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
