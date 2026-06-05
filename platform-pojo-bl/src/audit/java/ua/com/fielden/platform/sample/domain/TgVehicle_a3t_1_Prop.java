// Generation timestamp: 2025-05-07 11:15:11 EEST
package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.annotations.metamodel.WithoutMetaModel;
import ua.com.fielden.platform.audit.AbstractAuditEntity;
import ua.com.fielden.platform.audit.AbstractAuditProp;
import ua.com.fielden.platform.audit.AbstractSynAuditEntity;
import ua.com.fielden.platform.audit.annotations.AuditFor;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionIsGenerated;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DenyIntrospection;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.processors.verify.annotation.SkipVerification;

@EntityTitle("Tg Vehicle Audit 1 Changed Property")
@KeyTitle("Tg Vehicle Audit and Changed Property")
@MapEntityTo
@AuditFor(
    value = TgVehicle.class,
    version = 1
)
@CompanionIsGenerated
@SkipVerification
@SkipEntityRegistration
@WithoutMetaModel
@DenyIntrospection
@KeyType(DynamicEntityKey.class)
public class TgVehicle_a3t_1_Prop extends AbstractAuditProp<TgVehicle> {
  @CompositeKeyMember(1)
  @MapTo
  @Title(
      value = "Tg Vehicle Audit",
      desc = "The audit event associated with this changed property."
  )
  @IsProperty
  private TgVehicle_a3t_1 auditEntity;

  @CompositeKeyMember(2)
  @MapTo
  @Title(
      value = "Changed Property",
      desc = "The property that was changed as part of the audit event."
  )
  @IsProperty(ReTgVehicle_a3t.class)
  private PropertyDescriptor<ReTgVehicle_a3t> property;

  public TgVehicle_a3t_1 getAuditEntity() {
    return this.auditEntity;
  }

  @Observable
  public TgVehicle_a3t_1_Prop setAuditEntity(final TgVehicle_a3t_1 auditEntity) {
    this.auditEntity = auditEntity;
    return this;
  }

  public TgVehicle_a3t_1_Prop setAuditEntity(final AbstractAuditEntity<TgVehicle> auditEntity) {
    return setAuditEntity((TgVehicle_a3t_1) auditEntity);
  }

  public PropertyDescriptor<AbstractSynAuditEntity<TgVehicle>> getProperty() {
    return (PropertyDescriptor) this.property;
  }

  @Observable
  public TgVehicle_a3t_1_Prop setProperty(final PropertyDescriptor property) {
    this.property = property;
    return this;
  }
}
