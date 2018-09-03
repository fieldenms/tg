package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * Producer for {@link EntityEditAction}.
 *
 * @author TG Team
 *
 */
public class EntityEditActionProducer extends AbstractEntityEditActionProducer<EntityEditAction> {

    @Inject
    public EntityEditActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, EntityEditAction.class, companionFinder);
    }
}
