package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

///
/// Represents menu item for embedded entity centre.
///
@EntityTitle("Generic Audit Centre Menu Item")
@CompanionObject(AuditCompoundMenuItemCo.class)
@KeyType(PersistentEntityInfo.class)
public class AuditCompoundMenuItem extends AbstractPolymorphicCentreCompoundMenuItem<PersistentEntityInfo> {
}
