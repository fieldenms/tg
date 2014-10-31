package ua.com.fielden.platform.gis.gps;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.gis.gps.IMachineServerState;
import ua.com.fielden.platform.gis.gps.mixin.MachineServerStateMixin;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/** 
 * RAO implementation for master object {@link IMachineServerState} based on a common with DAO mixin.
 * 
 * @author Developers
 *
 */
@EntityType(MachineServerState.class)
public class MachineServerStateRao extends CommonEntityRao<MachineServerState> implements IMachineServerState {

    
    private final MachineServerStateMixin mixin;
    
    @Inject
    public MachineServerStateRao(final RestClientUtil restUtil) {
        super(restUtil);
        
        mixin = new MachineServerStateMixin(this);
    }
    
}