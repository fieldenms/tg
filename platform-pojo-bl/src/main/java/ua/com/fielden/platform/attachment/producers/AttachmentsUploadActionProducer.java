package ua.com.fielden.platform.attachment.producers;

import com.google.inject.Inject;

import ua.com.fielden.platform.attachment.AttachmentsUploadAction;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * Producer of {@link AttachmentsUploadAction} that populates its master entity if that entity is present in the context.
 * 
 * @author TG Team
 *
 */
public class AttachmentsUploadActionProducer extends DefaultEntityProducerWithContext<AttachmentsUploadAction> {

    @Inject
    public AttachmentsUploadActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, AttachmentsUploadAction.class, companionFinder);
    }

    @Override
    protected AttachmentsUploadAction provideDefaultValues(final AttachmentsUploadAction entity) {
        if (masterEntityNotEmpty() && masterEntityInstanceOf(AbstractPersistentEntity.class)) {
            entity.setMasterEntity((AbstractPersistentEntity<?>) masterEntity());
        } else if (selectedEntitiesOnlyOne()) {
            final AbstractPersistentEntity<?> selected = (AbstractPersistentEntity<?>) selectedEntities().get(0);
            entity.setMasterEntity(selected);
        }
        return entity;
    }
}
