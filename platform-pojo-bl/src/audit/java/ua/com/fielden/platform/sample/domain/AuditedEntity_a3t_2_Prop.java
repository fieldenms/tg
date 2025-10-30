// Generation timestamp: 2025-09-01 15:36:07 EEST
package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.annotations.metamodel.WithoutMetaModel;
import ua.com.fielden.platform.audit.AbstractAuditEntity;
import ua.com.fielden.platform.audit.AbstractAuditProp;
import ua.com.fielden.platform.audit.AbstractSynAuditEntity;
import ua.com.fielden.platform.audit.AuditFor;
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

@EntityTitle("Audited Entity Audit 2 Changed Property")
@KeyTitle("Audited Entity Audit and Changed Property")
@MapEntityTo
@AuditFor(
    value = AuditedEntity.class,
    version = 2
)
@CompanionIsGenerated
@SkipVerification
@SkipEntityRegistration
@WithoutMetaModel
@DenyIntrospection
@KeyType(DynamicEntityKey.class)
public class AuditedEntity_a3t_2_Prop extends AbstractAuditProp<AuditedEntity> {
  @CompositeKeyMember(1)
  @MapTo
  @Title(
      value = "Audited Entity Audit",
      desc = "The audit event associated with this changed property."
  )
  @IsProperty
  private AuditedEntity_a3t_2 auditEntity;

  @CompositeKeyMember(2)
  @MapTo
  @Title(
      value = "Changed Property",
      desc = "The property that was changed as part of the audit event."
  )
  @IsProperty(ReAuditedEntity_a3t.class)
  private PropertyDescriptor<ReAuditedEntity_a3t> property;

  public AuditedEntity_a3t_2 getAuditEntity() {
    return this.auditEntity;
  }

  @Observable
  public AuditedEntity_a3t_2_Prop setAuditEntity(final AuditedEntity_a3t_2 auditEntity) {
    this.auditEntity = auditEntity;
    return this;
  }

  public AuditedEntity_a3t_2_Prop setAuditEntity(
      final AbstractAuditEntity<AuditedEntity> auditEntity) {
    return setAuditEntity((AuditedEntity_a3t_2) auditEntity);
  }

  public PropertyDescriptor<AbstractSynAuditEntity<AuditedEntity>> getProperty() {
    return (PropertyDescriptor) this.property;
  }

  @Observable
  public AuditedEntity_a3t_2_Prop setProperty(final PropertyDescriptor property) {
    this.property = property;
    return this;
  }
}
