package ua.com.fielden.platform.sample.domain.compound.producers;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityChild;

/**
 * A producer for new instances of entity {@link TgCompoundEntityChild}.
 *
 * @author TG Team
 *
 */
public class TgCompoundEntityChildProducer extends DefaultEntityProducerWithContext<TgCompoundEntityChild> {

    @Inject
    public TgCompoundEntityChildProducer(final EntityFactory factory, final ICompanionObjectFinder coFinder) {
        super(factory, TgCompoundEntityChild.class, coFinder);
    }

    @Override
    protected TgCompoundEntityChild provideDefaultValuesForStandardNew(final TgCompoundEntityChild entityIn, final EntityNewAction masterEntity) {
        final TgCompoundEntityChild entityOut = super.provideDefaultValuesForStandardNew(entityIn, masterEntity);
        // This producer can be invoked from two places:
        // 1. Standalone centre
        // 2. Centre embedded in TgCompoundEntity Master
        // In the second case we want to default the tgCompoundEntity and make it read-only
        if (ofMasterEntity().keyOfMasterEntityInstanceOf(TgCompoundEntity.class)) {
            final TgCompoundEntity shallowTgCompoundEntity = ofMasterEntity().keyOfMasterEntity(TgCompoundEntity.class);
            // shallowTgCompoundEntity has been fetched in OpenTgCompoundEntityMasterActionProducer with key and desc only
            // It needs to be re-fetched here using a slightly deeper fetch model, as appropriate for CocEntry
            entityOut.setTgCompoundEntity(refetch(shallowTgCompoundEntity, "tgCompoundEntity"));
            entityOut.getProperty("tgCompoundEntity").validationResult().ifFailure(Result::throwRuntime);
            entityOut.getProperty("tgCompoundEntity").setEditable(false);
        }
        return entityOut;
    }

}