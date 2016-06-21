package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
/** 
 * DAO implementation for companion object {@link ITgMessage}.
 * 
 * @author Developers
 *
 */
@EntityType(TgMessage.class)
public class TgMessageDao extends CommonEntityDao<TgMessage> implements ITgMessage {

    @Inject
    public TgMessageDao(final IFilter filter) {
        super(filter);
    }

}