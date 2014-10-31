package ua.com.fielden.platform.gis.gps.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.gis.gps.IMachineServerState;
import ua.com.fielden.platform.gis.gps.MachineServerState;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;

/** 
 * Mixin implementation for companion object {@link IMachineServerState}.
 * 
 * @author Developers
 *
 */
public class MachineServerStateMixin {
    
    private final IMachineServerState companion;
    
    public MachineServerStateMixin(final IMachineServerState companion) {
        this.companion = companion;
    }
    
}