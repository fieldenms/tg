package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotationForClass;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.annotation.RestrictCreationByUsers;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_master.exceptions.SimpleMasterException;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;

/**
 * Producer for {@link EntityNewAction}.
 *
 * @author TG Team
 *
 */
public class EntityNewActionProducer extends EntityManipulationActionProducer<EntityNewAction> {

    @Inject
    public EntityNewActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final IAuthorisationModel authorisation, final ISecurityTokenProvider securityTokenProvider) {
        super(factory, EntityNewAction.class, companionFinder, authorisation, securityTokenProvider);
    }

    @Override
    protected EntityNewAction provideDefaultValues(final EntityNewAction entity) {
        final EntityNewAction updatedEntity = super.provideDefaultValues(entity);
        final RestrictCreationByUsers restrictUserCreation = getAnnotationForClass(RestrictCreationByUsers.class, updatedEntity.getEntityTypeAsClass());
        if (restrictUserCreation != null) {
            throw new SimpleMasterException(restrictUserCreation.value());
        }
        return updatedEntity;
    }
}