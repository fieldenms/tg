package ua.com.fielden.platform.entity.functional.master;

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

    @Inject
    public AcknowledgeWarningsDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    public AcknowledgeWarnings save(final AcknowledgeWarnings entity) {
        System.out.println("Master entity for continuation: " + entity.getContext().getMasterEntity());
        
        return super.save(entity);
    }
}