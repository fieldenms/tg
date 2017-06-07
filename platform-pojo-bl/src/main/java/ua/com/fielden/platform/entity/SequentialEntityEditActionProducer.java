package ua.com.fielden.platform.entity;

import java.util.List;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.web.centre.CentreContext;

public class SequentialEntityEditActionProducer extends EntityManipulationActionProducer<SequentialEntityEditAction> {

    @Inject
    public SequentialEntityEditActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, SequentialEntityEditAction.class, companionFinder);
    }

    @Override
    protected SequentialEntityEditAction provideDefaultValues(final SequentialEntityEditAction entity) {
        final SequentialEntityEditAction sequentialEditEntity = super.provideDefaultValues(entity);
        if (sequentialEditEntity.getContext() != null) {
            final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = (CentreContext<AbstractEntity<?>, AbstractEntity<?>>) sequentialEditEntity.getContext();
            final List<Long> entitiesToEdit = context.getSelectedEntities().stream().map(e -> e.getId()).collect(Collectors.toList());
            sequentialEditEntity.setEntitiesToEdit(entitiesToEdit);
        }
        return sequentialEditEntity;
    }
}
