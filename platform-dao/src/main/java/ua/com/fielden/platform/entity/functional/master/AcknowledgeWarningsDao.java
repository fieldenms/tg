package ua.com.fielden.platform.entity.functional.master;

import static java.lang.String.format;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.functional.master.AcknowledgeWarnings;
import ua.com.fielden.platform.entity.functional.master.IAcknowledgeWarnings;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link IAcknowledgeWarnings}.
 * 
 * @author Developers
 *
 */
@EntityType(AcknowledgeWarnings.class)
public class AcknowledgeWarningsDao extends CommonEntityDao<AcknowledgeWarnings> implements IAcknowledgeWarnings {
    private static final Logger LOGGER = Logger.getLogger(AcknowledgeWarningsDao.class);
    
    @Inject
    public AcknowledgeWarningsDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    public AcknowledgeWarnings save(final AcknowledgeWarnings entity) {
        LOGGER.debug(format("Master entity for continuation: [%s]", entity.getContext().getMasterEntity()));
        
        return super.save(entity);
    }
}