package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/// Master entity object to model the main menu item of the {@link PersistentEntityInfo} compound master.
///
@KeyType(PersistentEntityInfo.class)
@EntityTitle("Persistent Entity Version Info Main Menu Item")
@CompanionObject(PersistentEntityInfoMaster_OpenMain_MenuItemCo.class)
public class PersistentEntityInfoMaster_OpenMain_MenuItem extends AbstractFunctionalEntityForCompoundMenuItem<PersistentEntityInfo> {
}
