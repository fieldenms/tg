package ua.com.fielden.platform.sample.domain.compound.producers;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityDetail;
import ua.com.fielden.platform.sample.domain.compound.exceptions.CompoundModuleException;

/**
 * A producer for new instances of entity {@link TgCompoundEntityDetail}.
 *
 * @author TG Team
 *
 */
public class TgCompoundEntityDetailProducer extends DefaultEntityProducerWithContext<TgCompoundEntityDetail> {

    @Inject
    public TgCompoundEntityDetailProducer(final EntityFactory factory, final ICompanionObjectFinder coFinder) {
        super(factory, TgCompoundEntityDetail.class, coFinder);
    }

    @Override
    protected TgCompoundEntityDetail provideDefaultValues(final TgCompoundEntityDetail entity) {
        if (keyOfMasterEntityInstanceOf(TgCompoundEntity.class)) {
            final TgCompoundEntity instance = keyOfMasterEntity(TgCompoundEntity.class);
            return co$(TgCompoundEntityDetail.class).findByKeyAndFetch(co$(TgCompoundEntityDetail.class).getFetchProvider().fetchModel(), instance);
        } else {
            throw new CompoundModuleException("Not supported.");
        }
    }

}