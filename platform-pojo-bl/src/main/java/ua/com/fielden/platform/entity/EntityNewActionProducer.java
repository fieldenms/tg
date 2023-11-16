package ua.com.fielden.platform.entity;

import static java.lang.String.format;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.annotation.RestrictCreationByUsers;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_master.exceptions.SimpleMasterException;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;

/**
 * Producer for {@link EntityNewAction}.
 *
 * @author TG Team
 *
 */
public class EntityNewActionProducer extends EntityManipulationActionProducer<EntityNewAction> {

    public static final String ERR_ENTITY_CAN_NOT_BE_CREATED = "%s entity can not be created due to presence of %s annotation.";

    @Inject
    public EntityNewActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final IAuthorisationModel authorisation, final ISecurityTokenProvider securityTokenProvider) {
        super(factory, EntityNewAction.class, companionFinder, authorisation, securityTokenProvider);
    }

    @Override
    protected EntityNewAction provideDefaultValues(final EntityNewAction entity) {
        final EntityNewAction updatedEntity = super.provideDefaultValues(entity);
        if (AnnotationReflector.isAnnotationPresentForClass(RestrictCreationByUsers.class, updatedEntity.getEntityTypeAsClass())) {
            throw new SimpleMasterException(format(ERR_ENTITY_CAN_NOT_BE_CREATED, updatedEntity.getEntityTypeAsClass().getSimpleName(), RestrictCreationByUsers.class.getSimpleName()));
        }
        return updatedEntity;
    }
}