package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.security.user.ui_actions.OpenUserMasterActionCo;

@KeyType(Entity.class)
@CompanionObject(OpenUserMasterActionCo.class)
@EntityTitle("User Master")
public class OpenEntityMasterAction extends AbstractFunctionalEntityToOpenCompoundMaster<Entity> {}
