package fielden.close_leave.producers;

import com.google.inject.Inject;

import fielden.close_leave.TgCloseLeaveExample;
import fielden.close_leave.TgCloseLeaveExampleDetail;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/** 
 * A producer for new instances of entity {@link TgCloseLeaveExampleDetail}.
 * 
 * @author Developers
 *
 */
public class TgCloseLeaveExampleDetailProducer extends DefaultEntityProducerWithContext<TgCloseLeaveExampleDetail> {

    @Inject
    public TgCloseLeaveExampleDetailProducer(final EntityFactory factory, final ICompanionObjectFinder coFinder) {
        super(factory, TgCloseLeaveExampleDetail.class, coFinder);
    }

    @Override
    protected TgCloseLeaveExampleDetail provideDefaultValues(final TgCloseLeaveExampleDetail entity) {
        if (keyOfMasterEntityInstanceOf(TgCloseLeaveExample.class)) {
            return co$(TgCloseLeaveExampleDetail.class).findByKeyAndFetch(
                co$(TgCloseLeaveExampleDetail.class).getFetchProvider().fetchModel(),
                keyOfMasterEntity(TgCloseLeaveExample.class)
            );
        }
        return entity;
    }

}