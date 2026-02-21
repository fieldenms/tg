// Generation timestamp: 2025-09-01 15:36:07 EEST
package ua.com.fielden.platform.sample.domain;

import java.lang.String;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.audit.AbstractSynAuditEntity;
import ua.com.fielden.platform.audit.annotations.AuditFor;
import ua.com.fielden.platform.audit.annotations.InactiveAuditProperty;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionIsGenerated;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.DenyIntrospection;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.processors.verify.annotation.SkipVerification;
import ua.com.fielden.platform.types.RichText;

@AuditFor(AuditedEntity.class)
@EntityTitle("Audited Entity Audit")
@SkipVerification
@SkipEntityRegistration
@CompanionIsGenerated
@KeyType(DynamicEntityKey.class)
public class ReAuditedEntity_a3t extends AbstractSynAuditEntity<AuditedEntity> {
  protected static List<EntityResultQueryModel<ReAuditedEntity_a3t>> models_;

  @CompositeKeyMember(1)
  @Title(
      value = "Audited Entity",
      desc = "The audited Audited Entity."
  )
  @DenyIntrospection
  @IsProperty
  private AuditedEntity auditedEntity;

  @Title(
      value = "Changed Properties",
      desc = "Properties changed as part of the audited event."
  )
  @DenyIntrospection
  @IsProperty(ReAuditedEntity_a3t_Prop.class)
  private final Set<ReAuditedEntity_a3t_Prop> changedProps = new HashSet<>();

  @Title(
      value = "Changed Properties",
      desc = "Properties changed as part of the audited event."
  )
  @CritOnly(CritOnly.Type.MULTI)
  @DenyIntrospection
  @IsProperty(ReAuditedEntity_a3t.class)
  private PropertyDescriptor<ReAuditedEntity_a3t> changedPropsCrit;

  @Title(
      value = "Bool 1",
      desc = "[Bool 1] at the time of the audited event."
  )
  @IsProperty
  private boolean a3t_bool1;

  @Title(
      value = "Date 1",
      desc = "[Date 1] at the time of the audited event."
  )
  @IsProperty
  private Date a3t_date1;

  @Title(
      value = "Invalidate",
      desc = "[Invalidate] at the time of the audited event."
  )
  @IsProperty
  private boolean a3t_invalidate;

  @Title(
      value = "Key",
      desc = "[Key] at the time of the audited event."
  )
  @IsProperty
  private String a3t_key;

  @Title(
      value = "Rich Text",
      desc = "[Rich Text] at the time of the audited event."
  )
  @IsProperty
  private RichText a3t_richText;

  @Title(
      value = "Str 1 [removed]",
      desc = "[Str 1] at the time of the audited event."
  )
  @InactiveAuditProperty
  @IsProperty
  private String a3t_str1;

  @Title(
      value = "Str 2",
      desc = "[Str 2] at the time of the audited event."
  )
  @IsProperty
  private String a3t_str2;

  @Title(
      value = "Union Entity",
      desc = "[Union Entity] at the time of the audited event."
  )
  @IsProperty
  private UnionEntity a3t_union;

  public AuditedEntity getAuditedEntity() {
    return this.auditedEntity;
  }

  @Observable
  public ReAuditedEntity_a3t setAuditedEntity(final AuditedEntity auditedEntity) {
    this.auditedEntity = auditedEntity;
    return this;
  }

  public Set<ReAuditedEntity_a3t_Prop> getChangedProps() {
    return Collections.unmodifiableSet(this.changedProps);
  }

  @Observable
  public ReAuditedEntity_a3t setChangedProps(final Set<ReAuditedEntity_a3t_Prop> changedProps) {
    this.changedProps.clear();
    this.changedProps.addAll(changedProps);
    return this;
  }

  public PropertyDescriptor<ReAuditedEntity_a3t> getChangedPropsCrit() {
    return this.changedPropsCrit;
  }

  @Observable
  public ReAuditedEntity_a3t setChangedPropsCrit(
      final PropertyDescriptor<ReAuditedEntity_a3t> changedPropsCrit) {
    this.changedPropsCrit = changedPropsCrit;
    return this;
  }

  public boolean isA3t_bool1() {
    return this.a3t_bool1;
  }

  @Observable
  public ReAuditedEntity_a3t setA3t_bool1(final boolean a3t_bool1) {
    this.a3t_bool1 = a3t_bool1;
    return this;
  }

  public Date getA3t_date1() {
    return this.a3t_date1;
  }

  @Observable
  public ReAuditedEntity_a3t setA3t_date1(final Date a3t_date1) {
    this.a3t_date1 = a3t_date1;
    return this;
  }

  public boolean isA3t_invalidate() {
    return this.a3t_invalidate;
  }

  @Observable
  public ReAuditedEntity_a3t setA3t_invalidate(final boolean a3t_invalidate) {
    this.a3t_invalidate = a3t_invalidate;
    return this;
  }

  public String getA3t_key() {
    return this.a3t_key;
  }

  @Observable
  public ReAuditedEntity_a3t setA3t_key(final String a3t_key) {
    this.a3t_key = a3t_key;
    return this;
  }

  public RichText getA3t_richText() {
    return this.a3t_richText;
  }

  @Observable
  public ReAuditedEntity_a3t setA3t_richText(final RichText a3t_richText) {
    this.a3t_richText = a3t_richText;
    return this;
  }

  public String getA3t_str1() {
    return this.a3t_str1;
  }

  @Observable
  public ReAuditedEntity_a3t setA3t_str1(final String a3t_str1) {
    this.a3t_str1 = a3t_str1;
    return this;
  }

  public String getA3t_str2() {
    return this.a3t_str2;
  }

  @Observable
  public ReAuditedEntity_a3t setA3t_str2(final String a3t_str2) {
    this.a3t_str2 = a3t_str2;
    return this;
  }

  public UnionEntity getA3t_union() {
    return this.a3t_union;
  }

  @Observable
  public ReAuditedEntity_a3t setA3t_union(final UnionEntity a3t_union) {
    this.a3t_union = a3t_union;
    return this;
  }
}
