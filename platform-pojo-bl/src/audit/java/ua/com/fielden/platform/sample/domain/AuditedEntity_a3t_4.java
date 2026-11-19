// Generation timestamp: 2026-11-19 00:00:04 AEDT
package ua.com.fielden.platform.sample.domain;

import java.lang.String;
import java.util.Date;
import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.annotations.metamodel.WithoutMetaModel;
import ua.com.fielden.platform.audit.annotations.AuditFor;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionIsGenerated;
import ua.com.fielden.platform.entity.annotation.DenyIntrospection;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.processors.verify.annotation.SkipVerification;

@AuditFor(
    value = AuditedEntity.class,
    version = 4
)
@MapEntityTo
@CompanionIsGenerated
@EntityTitle("Audited Entity Audit 4")
@SkipVerification
@SkipEntityRegistration
@WithoutMetaModel
@KeyType(DynamicEntityKey.class)
@DenyIntrospection
public class AuditedEntity_a3t_4 extends AuditedEntity_a3t_3 {
  @IsProperty
  @MapTo
  @Final
  @Invisible(
      centreOnly = true
  )
  @Title(
      value = "Date 1",
      desc = "[Date 1] at the time of the audited event."
  )
  private Date a3t_date1;

  @IsProperty
  @MapTo
  @Final
  @Title(
      value = "Note",
      desc = "[Note] at the time of the audited event."
  )
  private String a3t_note;

  @IsProperty
  @MapTo
  @Final
  @Invisible
  @Title(
      value = "Secret",
      desc = "[Secret] at the time of the audited event."
  )
  private String a3t_secret;

  @IsProperty
  @MapTo
  @Final
  @Invisible
  @Title(
      value = "Token",
      desc = "[Token] at the time of the audited event."
  )
  private String a3t_token;

  public Date getA3t_date1() {
    return this.a3t_date1;
  }

  @Observable
  public AuditedEntity_a3t_4 setA3t_date1(final Date a3t_date1) {
    this.a3t_date1 = a3t_date1;
    return this;
  }

  public String getA3t_note() {
    return this.a3t_note;
  }

  @Observable
  public AuditedEntity_a3t_4 setA3t_note(final String a3t_note) {
    this.a3t_note = a3t_note;
    return this;
  }

  public String getA3t_secret() {
    return this.a3t_secret;
  }

  @Observable
  public AuditedEntity_a3t_4 setA3t_secret(final String a3t_secret) {
    this.a3t_secret = a3t_secret;
    return this;
  }

  public String getA3t_token() {
    return this.a3t_token;
  }

  @Observable
  public AuditedEntity_a3t_4 setA3t_token(final String a3t_token) {
    this.a3t_token = a3t_token;
    return this;
  }
}
