package ua.com.fielden.platform.entity.functional.master;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link AcknowledgeWarningsCo}.
 * 
 * @author TG Team
 *
 */
@EntityType(AcknowledgeWarnings.class)
public class AcknowledgeWarningsDao extends CommonEntityDao<AcknowledgeWarnings> implements AcknowledgeWarningsCo {
    
    @Inject
    public AcknowledgeWarningsDao(final IFilter filter) {
        super(filter);
    }
    
}