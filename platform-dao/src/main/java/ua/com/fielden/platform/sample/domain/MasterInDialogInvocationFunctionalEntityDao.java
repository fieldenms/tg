package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import java.util.Map;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link IMasterInDialogInvocationFunctionalEntity}.
 * 
 * @author Developers
 *
 */
@EntityType(MasterInDialogInvocationFunctionalEntity.class)
public class MasterInDialogInvocationFunctionalEntityDao extends CommonEntityDao<MasterInDialogInvocationFunctionalEntity> implements IMasterInDialogInvocationFunctionalEntity {
    @Inject
    public MasterInDialogInvocationFunctionalEntityDao(final IFilter filter) {
        super(filter);
    }

}