package fielden.test_app.close_leave.producers;

import com.google.inject.Inject;

import fielden.test_app.close_leave.TgCloseLeaveExample;
import fielden.test_app.close_leave.TgCloseLeaveExampleDetailUnpersisted;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/** 
 * A producer for new instances of entity {@link TgCloseLeaveExampleDetailUnpersisted}.
 * 
 * @author Developers
 *
 */
public class TgCloseLeaveExampleDetailUnpersistedProducer extends DefaultEntityProducerWithContext<TgCloseLeaveExampleDetailUnpersisted> {

    @Inject
    public TgCloseLeaveExampleDetailUnpersistedProducer(final EntityFactory factory, final ICompanionObjectFinder coFinder) {
        super(factory, TgCloseLeaveExampleDetailUnpersisted.class, coFinder);
    }

    @Override
    protected TgCloseLeaveExampleDetailUnpersisted provideDefaultValues(final TgCloseLeaveExampleDetailUnpersisted entity) {
        if (keyOfMasterEntityInstanceOf(TgCloseLeaveExample.class)) {
            final TgCloseLeaveExampleDetailUnpersisted fetchedEntity = co$(TgCloseLeaveExampleDetailUnpersisted.class).findByKeyAndFetch(
                co$(TgCloseLeaveExampleDetailUnpersisted.class).getFetchProvider().fetchModel(),
                keyOfMasterEntity(TgCloseLeaveExample.class)
            );
            if (fetchedEntity != null) {
                return fetchedEntity;
            }
            entity.beginInitialising();
            entity.setKey(keyOfMasterEntity(TgCloseLeaveExample.class));
            entity.endInitialising();
            return entity;
        }
        return entity;
    }

}