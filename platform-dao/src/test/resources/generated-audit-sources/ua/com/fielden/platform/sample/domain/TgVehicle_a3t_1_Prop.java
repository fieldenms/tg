package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.audit.AbstractAuditProp;
import ua.com.fielden.platform.audit.AuditPropFor;
import ua.com.fielden.platform.entity.annotation.CompanionIsGenerated;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

@MapEntityTo
@AuditPropFor(TgVehicle_a3t_1.class)
@CompanionIsGenerated
public class TgVehicle_a3t_1_Prop extends AbstractAuditProp<TgVehicle_a3t_1> {
  @CompositeKeyMember(1)
  @MapTo
  @IsProperty
  private TgVehicle_a3t_1 auditEntity;

  @CompositeKeyMember(2)
  @MapTo
  @IsProperty(TgVehicle_a3t_1.class)
  private PropertyDescriptor<TgVehicle_a3t_1> property;

  public TgVehicle_a3t_1 getAuditEntity() {
    return this.auditEntity;
  }

  @Observable
  public TgVehicle_a3t_1_Prop setAuditEntity(final TgVehicle_a3t_1 auditEntity) {
    this.auditEntity = auditEntity;
    return this;
  }

  public PropertyDescriptor<TgVehicle_a3t_1> getProperty() {
    return this.property;
  }

  @Observable
  public TgVehicle_a3t_1_Prop setProperty(final PropertyDescriptor<TgVehicle_a3t_1> property) {
    this.property = property;
    return this;
  }
}
