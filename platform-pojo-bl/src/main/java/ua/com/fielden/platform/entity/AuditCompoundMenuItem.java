package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/// Represents a menu item for an embedded polymorphic audit entity centre.
///
@EntityTitle("Generic Audit Centre Menu Item")
@CompanionObject(AuditCompoundMenuItemCo.class)
@KeyType(PersistentEntityInfo.class)
public class AuditCompoundMenuItem extends AbstractPolymorphicCentreCompoundMenuItem<PersistentEntityInfo> {
}
