package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link ITgAcknowledgeWarnings}.
 * 
 * @author Developers
 *
 */
@EntityType(TgAcknowledgeWarnings.class)
public class TgAcknowledgeWarningsDao extends CommonEntityDao<TgAcknowledgeWarnings> implements ITgAcknowledgeWarnings {

    @Inject
    public TgAcknowledgeWarningsDao(final IFilter filter) {
        super(filter);
    }

}