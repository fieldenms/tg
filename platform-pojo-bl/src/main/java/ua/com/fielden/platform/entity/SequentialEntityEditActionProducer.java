package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

public class SequentialEntityEditActionProducer extends AbstractEntityEditActionProducer<SequentialEntityEditAction> {

    @Inject
    public SequentialEntityEditActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, SequentialEntityEditAction.class, companionFinder);
    }
}
