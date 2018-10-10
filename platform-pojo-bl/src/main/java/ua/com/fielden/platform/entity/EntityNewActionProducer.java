package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

import com.google.inject.Inject;

/**
 * Producer for {@link EntityNewAction}.
 * 
 * @author TG Team
 *
 */
public class EntityNewActionProducer extends EntityManipulationActionProducer<EntityNewAction> {
    
    @Inject
    public EntityNewActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, EntityNewAction.class, companionFinder);
    }
    
}
