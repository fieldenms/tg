package ua.com.fielden.platform.gis.gps;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link IMachineServerState}.
 * 
 * @author Developers
 *
 */
@EntityType(MachineServerState.class)
public class MachineServerStateDao extends CommonEntityDao<MachineServerState> implements IMachineServerState {
    
    @Inject
    public MachineServerStateDao(final IFilter filter) {
        super(filter);
    }
    
}