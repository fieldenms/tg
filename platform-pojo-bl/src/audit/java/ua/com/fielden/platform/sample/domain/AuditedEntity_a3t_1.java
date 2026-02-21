// Generation timestamp: 2025-09-01 15:29:15 EEST
package ua.com.fielden.platform.sample.domain;

import java.lang.String;
import java.util.Date;
import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.annotations.metamodel.WithoutMetaModel;
import ua.com.fielden.platform.audit.AbstractAuditEntity;
import ua.com.fielden.platform.audit.annotations.AuditFor;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionIsGenerated;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DenyIntrospection;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.processors.verify.annotation.SkipVerification;

@AuditFor(
    value = AuditedEntity.class,
    version = 1
)
@MapEntityTo
@CompanionIsGenerated
@EntityTitle("Audited Entity Audit 1")
@SkipVerification
@SkipEntityRegistration
@WithoutMetaModel
@KeyType(DynamicEntityKey.class)
@DenyIntrospection
public class AuditedEntity_a3t_1 extends AbstractAuditEntity<AuditedEntity> {
  @IsProperty
  @MapTo
  @Final
  @Title(
      value = "Bool 1",
      desc = "[Bool 1] at the time of the audited event."
  )
  private boolean a3t_bool1;

  @IsProperty
  @MapTo
  @Final
  @Title(
      value = "Date 1",
      desc = "[Date 1] at the time of the audited event."
  )
  private Date a3t_date1;

  @IsProperty
  @MapTo
  @Final
  @Title(
      value = "Key",
      desc = "[Key] at the time of the audited event."
  )
  private String a3t_key;

  @IsProperty
  @MapTo
  @Final
  @Title(
      value = "Str 1",
      desc = "[Str 1] at the time of the audited event."
  )
  private String a3t_str1;

  @CompositeKeyMember(1)
  @MapTo
  @Required
  @Final
  @Title(
      value = "Audited Entity",
      desc = "The audited Audited Entity."
  )
  @SkipEntityExistsValidation
  @IsProperty
  private AuditedEntity auditedEntity;

  public boolean isA3t_bool1() {
    return this.a3t_bool1;
  }

  @Observable
  public AuditedEntity_a3t_1 setA3t_bool1(final boolean a3t_bool1) {
    this.a3t_bool1 = a3t_bool1;
    return this;
  }

  public Date getA3t_date1() {
    return this.a3t_date1;
  }

  @Observable
  public AuditedEntity_a3t_1 setA3t_date1(final Date a3t_date1) {
    this.a3t_date1 = a3t_date1;
    return this;
  }

  public String getA3t_key() {
    return this.a3t_key;
  }

  @Observable
  public AuditedEntity_a3t_1 setA3t_key(final String a3t_key) {
    this.a3t_key = a3t_key;
    return this;
  }

  public String getA3t_str1() {
    return this.a3t_str1;
  }

  @Observable
  public AuditedEntity_a3t_1 setA3t_str1(final String a3t_str1) {
    this.a3t_str1 = a3t_str1;
    return this;
  }

  public AuditedEntity getAuditedEntity() {
    return this.auditedEntity;
  }

  @Observable
  public AuditedEntity_a3t_1 setAuditedEntity(final AuditedEntity auditedEntity) {
    this.auditedEntity = auditedEntity;
    return this;
  }
}
