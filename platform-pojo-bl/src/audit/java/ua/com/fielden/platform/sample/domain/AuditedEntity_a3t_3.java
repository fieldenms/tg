// Generation timestamp: 2026-10-27 19:46:12 EET
package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.annotations.metamodel.WithoutMetaModel;
import ua.com.fielden.platform.audit.annotations.AuditFor;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionIsGenerated;
import ua.com.fielden.platform.entity.annotation.DenyIntrospection;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.processors.verify.annotation.SkipVerification;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;

@AuditFor(
    value = AuditedEntity.class,
    version = 3
)
@MapEntityTo
@CompanionIsGenerated
@EntityTitle("Audited Entity Audit 3")
@SkipVerification
@SkipEntityRegistration
@WithoutMetaModel
@KeyType(DynamicEntityKey.class)
@DenyIntrospection
public class AuditedEntity_a3t_3 extends AuditedEntity_a3t_2 {
  @IsProperty
  @MapTo
  @Final
  @Title(
      value = "Colour",
      desc = "[Colour] at the time of the audited event."
  )
  private Colour a3t_colour;

  @IsProperty
  @MapTo
  @Final
  @Title(
      value = "Hyperlink",
      desc = "[Hyperlink] at the time of the audited event."
  )
  private Hyperlink a3t_hyperlink;

  public Colour getA3t_colour() {
    return this.a3t_colour;
  }

  @Observable
  public AuditedEntity_a3t_3 setA3t_colour(final Colour a3t_colour) {
    this.a3t_colour = a3t_colour;
    return this;
  }

  public Hyperlink getA3t_hyperlink() {
    return this.a3t_hyperlink;
  }

  @Observable
  public AuditedEntity_a3t_3 setA3t_hyperlink(final Hyperlink a3t_hyperlink) {
    this.a3t_hyperlink = a3t_hyperlink;
    return this;
  }
}
