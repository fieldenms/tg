package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * Producer for {@link EntityNavigationAction}.
 *
 * @author TG Team
 *
 */
public class EntityNavigationActionProducer extends AbstractEntityEditActionProducer<EntityNavigationAction> {

    @Inject
    public EntityNavigationActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, EntityNavigationAction.class, companionFinder);
    }
}
