// Generation timestamp: 2026-11-19 00:00:21 AEDT
package ua.com.fielden.platform.sample.domain;

import java.lang.String;
import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.annotations.metamodel.WithoutMetaModel;
import ua.com.fielden.platform.audit.annotations.AuditFor;
import ua.com.fielden.platform.audit.annotations.InactiveAuditProperty;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionIsGenerated;
import ua.com.fielden.platform.entity.annotation.DenyIntrospection;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.processors.verify.annotation.SkipVerification;

@AuditFor(
    value = AuditedEntity.class,
    version = 5
)
@MapEntityTo
@CompanionIsGenerated
@EntityTitle("Audited Entity Audit 5")
@SkipVerification
@SkipEntityRegistration
@WithoutMetaModel
@KeyType(DynamicEntityKey.class)
@DenyIntrospection
public class AuditedEntity_a3t_5 extends AuditedEntity_a3t_4 {
  @IsProperty
  @InactiveAuditProperty
  @Invisible
  @Title(
      value = "Note [removed]",
      desc = "[Note] at the time of the audited event."
  )
  private String a3t_note;

  @IsProperty
  @InactiveAuditProperty
  @Invisible
  @Title(
      value = "Token [removed]",
      desc = "[Token] at the time of the audited event."
  )
  private String a3t_token;

  public String getA3t_note() {
    return this.a3t_note;
  }

  @Observable
  public AuditedEntity_a3t_5 setA3t_note(final String a3t_note) {
    this.a3t_note = a3t_note;
    return this;
  }

  public String getA3t_token() {
    return this.a3t_token;
  }

  @Observable
  public AuditedEntity_a3t_5 setA3t_token(final String a3t_token) {
    this.a3t_token = a3t_token;
    return this;
  }
}
