// Generation timestamp: 2025-03-25 16:16:54 EET
package ua.com.fielden.platform.sample.domain;

import java.util.List;
import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.annotations.metamodel.WithoutMetaModel;
import ua.com.fielden.platform.audit.AbstractSynAuditEntity;
import ua.com.fielden.platform.audit.AbstractSynAuditProp;
import ua.com.fielden.platform.audit.annotations.AuditFor;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionIsGenerated;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.processors.verify.annotation.SkipVerification;

@AuditFor(TgVehicle.class)
@SkipVerification
@SkipEntityRegistration
@CompanionIsGenerated
@WithoutMetaModel
@KeyType(DynamicEntityKey.class)
@EntityTitle("Tg Vehicle Audit Changed Property")
@KeyTitle("Tg Vehicle Audit and Changed Property")
public class ReTgVehicle_a3t_Prop extends AbstractSynAuditProp<TgVehicle> {
  protected static List<EntityResultQueryModel<ReTgVehicle_a3t_Prop>> models_;

  @CompositeKeyMember(1)
  @Title(
      value = "Tg Vehicle Audit",
      desc = "The audit event associated with this changed property."
  )
  @IsProperty
  private ReTgVehicle_a3t auditEntity;

  @CompositeKeyMember(2)
  @Title(
      value = "Changed Property",
      desc = "The property that was changed as part of the audit event."
  )
  @IsProperty(ReTgVehicle_a3t.class)
  private PropertyDescriptor<ReTgVehicle_a3t> property;

  public ReTgVehicle_a3t getAuditEntity() {
    return this.auditEntity;
  }

  @Observable
  public ReTgVehicle_a3t_Prop setAuditEntity(final ReTgVehicle_a3t auditEntity) {
    this.auditEntity = auditEntity;
    return this;
  }

  public ReTgVehicle_a3t_Prop setAuditEntity(final AbstractSynAuditEntity<TgVehicle> auditEntity) {
    return setAuditEntity((ReTgVehicle_a3t) auditEntity);
  }

  public PropertyDescriptor<AbstractSynAuditEntity<TgVehicle>> getProperty() {
    return (PropertyDescriptor) this.property;
  }

  @Observable
  public ReTgVehicle_a3t_Prop setProperty(final PropertyDescriptor property) {
    this.property = property;
    return this;
  }
}
