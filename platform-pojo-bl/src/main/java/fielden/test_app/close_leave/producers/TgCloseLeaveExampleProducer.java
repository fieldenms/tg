package fielden.test_app.close_leave.producers;

import com.google.inject.Inject;

import fielden.test_app.close_leave.TgCloseLeaveExample;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/** 
 * A producer for new instances of entity {@link TgCloseLeaveExample}.
 * 
 * @author Developers
 *
 */
public class TgCloseLeaveExampleProducer extends DefaultEntityProducerWithContext<TgCloseLeaveExample> {

    @Inject
    public TgCloseLeaveExampleProducer(final EntityFactory factory, final ICompanionObjectFinder coFinder) {
        super(factory, TgCloseLeaveExample.class, coFinder);
    }

    @Override
    protected TgCloseLeaveExample provideDefaultValues(final TgCloseLeaveExample entity) {
        if (keyOfMasterEntityInstanceOf(TgCloseLeaveExample.class)) {
            final TgCloseLeaveExample instance = keyOfMasterEntity(TgCloseLeaveExample.class);
            if (instance.isPersisted()) {
                return refetchInstrumentedEntityById(instance.getId());
            } else {
                return instance;
            }
        }
        return super.provideDefaultValues(entity);
    }

}