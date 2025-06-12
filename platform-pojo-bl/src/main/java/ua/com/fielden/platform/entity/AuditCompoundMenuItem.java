package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;

@EntityTitle("Generic Audit Centre Menu Item")
@CompanionObject(AuditCompoundMenuItemCo.class)
public class AuditCompoundMenuItem extends AbstractVolatileCentreCompoundMenuItem<AbstractEntity<?>> {
}
