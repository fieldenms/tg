package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
/** 
 * DAO implementation for companion object {@link ITgMachine}.
 * 
 * @author Developers
 *
 */
@EntityType(TgMachine.class)
public class TgMachineDao extends CommonEntityDao<TgMachine> implements ITgMachine {

    @Inject
    public TgMachineDao(final IFilter filter) {
        super(filter);
    }

}