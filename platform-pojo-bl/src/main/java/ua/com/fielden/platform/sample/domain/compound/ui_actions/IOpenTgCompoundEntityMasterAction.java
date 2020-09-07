package ua.com.fielden.platform.sample.domain.compound.ui_actions;

import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.dao.IEntityDao;

/** 
 * Companion object for entity {@link OpenTgCompoundEntityMasterAction}.
 * 
 * @author TG Team
 *
 */
public interface IOpenTgCompoundEntityMasterAction extends IEntityDao<OpenTgCompoundEntityMasterAction> {

    static final IFetchProvider<OpenTgCompoundEntityMasterAction> FETCH_PROVIDER = EntityUtils.fetch(OpenTgCompoundEntityMasterAction.class).with(
        // key is needed to be correctly autopopulated by newly saved compound master entity (ID-based restoration of entity-typed key)
        "key");

}