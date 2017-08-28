package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * A producer for new instances of entity {@link TgCentreInvokerWithCentreContext}.
 *
 * @author TG Team
 *
 */
public class TgCentreInvokerWithCentreContextProducer extends DefaultEntityProducerWithContext<TgCentreInvokerWithCentreContext> {
    private final Logger logger = Logger.getLogger(getClass());

    @Inject
    public TgCentreInvokerWithCentreContextProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgCentreInvokerWithCentreContext.class, companionFinder);
    }

    @Override
    protected TgCentreInvokerWithCentreContext provideDefaultValues(final TgCentreInvokerWithCentreContext entity) {
        logger.error("restored masterEntity (centre context's selection criteria): " + selectionCrit().get("tgPersistentEntityWithProperties_critOnlyBigDecimalProp"));
        logger.error("restored masterEntity (centre context's selection criteria): " + selectionCrit().get("tgPersistentEntityWithProperties_bigDecimalProp_from"));
        
        entity.setCritOnlyBigDecimalPropCriterion(selectionCrit().get("tgPersistentEntityWithProperties_critOnlyBigDecimalProp"));
        entity.setBigDecimalPropFromCriterion(selectionCrit().get("tgPersistentEntityWithProperties_bigDecimalProp_from"));
        return entity;
    }
}