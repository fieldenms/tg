package ua.com.fielden.platform.sample.domain.ui_actions.producers;

import static ua.com.fielden.platform.entity.validation.custom.DefaultEntityValidator.validateWithoutCritOnly;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.error.Result;
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
            final TgPersistentEntityWithProperties masterEntity = masterEntity(TgPersistentEntityWithProperties.class);
            // make sure that masterEntity is valid before continuing;
            // in case where some property was changed and action immediately started, validation is guaranteed to be started and completed;
            // this guarantees that invalid entity will be bound to the parent master;
            // whether validation completes earlier than MakeCompletedAction erroneous retrieval or later does not really matter
            masterEntity.isValid(validateWithoutCritOnly).ifFailure(Result::throwRuntime);
            entity.setMasterEntity(masterEntity);
        }
        return super.provideDefaultValues(entity);
    }

}