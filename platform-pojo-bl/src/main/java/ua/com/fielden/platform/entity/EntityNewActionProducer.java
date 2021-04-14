package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
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
    
}
