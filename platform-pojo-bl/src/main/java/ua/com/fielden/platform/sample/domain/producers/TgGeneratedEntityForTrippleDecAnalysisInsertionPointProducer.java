package ua.com.fielden.platform.sample.domain.producers;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.sample.domain.TgGeneratedEntityForTrippleDecAnalysisInsertionPoint;
/**
 * A producer for new instances of entity {@link TgGeneratedEntityForTrippleDecAnalysisInsertionPoint}.
 *
 * @author TG Team
 *
 */
public class TgGeneratedEntityForTrippleDecAnalysisInsertionPointProducer extends DefaultEntityProducerWithContext<TgGeneratedEntityForTrippleDecAnalysisInsertionPoint> {

    @Inject
    public TgGeneratedEntityForTrippleDecAnalysisInsertionPointProducer(final EntityFactory factory, final ICompanionObjectFinder coFinder) {
        super(factory, TgGeneratedEntityForTrippleDecAnalysisInsertionPoint.class, coFinder);
    }

    @Override
    protected TgGeneratedEntityForTrippleDecAnalysisInsertionPoint provideDefaultValues(final TgGeneratedEntityForTrippleDecAnalysisInsertionPoint entity) {
        return super.provideDefaultValues(entity);
    }

}