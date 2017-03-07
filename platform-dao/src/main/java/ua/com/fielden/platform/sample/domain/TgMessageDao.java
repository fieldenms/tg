package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.observables.TgMessageChangeSubject;
/** 
 * DAO implementation for companion object {@link ITgMessage}.
 * 
 * @author Developers
 *
 */
@EntityType(TgMessage.class)
public class TgMessageDao extends CommonEntityDao<TgMessage> implements ITgMessage {
    private final TgMessageChangeSubject changeSubject;

    @Inject
    public TgMessageDao(final IFilter filter, final TgMessageChangeSubject changeSubject) {
        super(filter);
        this.changeSubject = changeSubject;
    }
    
    @Override
    protected IFetchProvider<TgMessage> createFetchProvider() {
        return super.createFetchProvider().with("machine.desc", "gpsTime", "travelledDistance", "vectorAngle", "vectorSpeed", "x", "y");
    }
    
    @Override
    @SessionRequired
    public TgMessage save(final TgMessage entity) {
        final TgMessage savedMessage = super.save(entity);
        changeSubject.publish(savedMessage);
        return savedMessage;
    }

}