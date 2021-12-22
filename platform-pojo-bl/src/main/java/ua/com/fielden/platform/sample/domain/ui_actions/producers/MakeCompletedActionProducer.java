package ua.com.fielden.platform.sample.domain.ui_actions.producers;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.ui_actions.MakeCompletedAction;

/**
 * A producer for new instances of entity {@link MakeCompletedAction}.
 *
 * @author TG Team
 *
 */
public class MakeCompletedActionProducer extends DefaultEntityProducerWithContext<MakeCompletedAction> {

    @Inject
    public MakeCompletedActionProducer(final EntityFactory factory, final ICompanionObjectFinder coFinder) {
        super(factory, MakeCompletedAction.class, coFinder);
    }

    @Override
    protected MakeCompletedAction provideDefaultValues(final MakeCompletedAction entity) {
        if (contextNotEmpty() && masterEntityNotEmpty()) {
            entity.setMasterEntity(masterEntity(TgPersistentEntityWithProperties.class)); // defer masterEntity validation to companion's save method; this is to ensure proper entity binding in postActionSuccess/postActionError callbacks
        }
        return super.provideDefaultValues(entity);
    }

}