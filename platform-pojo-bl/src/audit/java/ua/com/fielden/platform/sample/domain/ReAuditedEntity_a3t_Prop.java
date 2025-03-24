// Generation timestamp: 2025-03-24 16:40:20 EET
package ua.com.fielden.platform.sample.domain;

import java.util.List;
import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.annotations.metamodel.WithoutMetaModel;
import ua.com.fielden.platform.audit.AbstractSynAuditEntity;
import ua.com.fielden.platform.audit.AbstractSynAuditProp;
import ua.com.fielden.platform.audit.AuditFor;
import ua.com.fielden.platform.audit.SynAuditPropEntityUtils;
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

@AuditFor(AuditedEntity.class)
@SkipVerification
@SkipEntityRegistration
@CompanionIsGenerated
@WithoutMetaModel
@KeyType(DynamicEntityKey.class)
@EntityTitle("Audited Entity Audit Changed Property")
@KeyTitle("Audited Entity Audit and Changed Property")
public class ReAuditedEntity_a3t_Prop extends AbstractSynAuditProp<AuditedEntity> {
  private static final EntityResultQueryModel<ReAuditedEntity_a3t_Prop> model_a3t_1 = SynAuditPropEntityUtils.modelAuditProp(AuditedEntity_a3t_1_Prop.class, ReAuditedEntity_a3t_Prop.class, AuditedEntity_a3t_1.class, ReAuditedEntity_a3t.class);

  private static final EntityResultQueryModel<ReAuditedEntity_a3t_Prop> model_a3t_2 = SynAuditPropEntityUtils.modelAuditProp(AuditedEntity_a3t_2_Prop.class, ReAuditedEntity_a3t_Prop.class, AuditedEntity_a3t_2.class, ReAuditedEntity_a3t.class);

  protected static final List<EntityResultQueryModel<ReAuditedEntity_a3t_Prop>> models_ = List.of(model_a3t_1, model_a3t_2);

  @CompositeKeyMember(1)
  @Title(
      value = "Audited Entity Audit",
      desc = "The audit event associated with this changed property."
  )
  @IsProperty
  private ReAuditedEntity_a3t auditEntity;

  @CompositeKeyMember(2)
  @Title(
      value = "Changed Property",
      desc = "The property that was changed as part of the audit event."
  )
  @IsProperty(ReAuditedEntity_a3t.class)
  private PropertyDescriptor<ReAuditedEntity_a3t> property;

  public ReAuditedEntity_a3t getAuditEntity() {
    return this.auditEntity;
  }

  @Observable
  public ReAuditedEntity_a3t_Prop setAuditEntity(final ReAuditedEntity_a3t auditEntity) {
    this.auditEntity = auditEntity;
    return this;
  }

  public ReAuditedEntity_a3t_Prop setAuditEntity(
      final AbstractSynAuditEntity<AuditedEntity> auditEntity) {
    return setAuditEntity((ReAuditedEntity_a3t) auditEntity);
  }

  public PropertyDescriptor<AbstractSynAuditEntity<AuditedEntity>> getProperty() {
    return (PropertyDescriptor) this.property;
  }

  @Observable
  public ReAuditedEntity_a3t_Prop setProperty(final PropertyDescriptor property) {
    this.property = property;
    return this;
  }
}
