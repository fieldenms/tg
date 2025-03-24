// Generation timestamp: 2025-03-24 16:40:20 EET
package ua.com.fielden.platform.sample.domain;

import java.lang.String;
import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.annotations.metamodel.WithoutMetaModel;
import ua.com.fielden.platform.audit.AuditFor;
import ua.com.fielden.platform.audit.InactiveAuditProperty;
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

@AuditFor(
    value = AuditedEntity.class,
    version = 2
)
@MapEntityTo
@CompanionIsGenerated
@EntityTitle("Audited Entity Audit")
@SkipVerification
@SkipEntityRegistration
@WithoutMetaModel
@KeyType(DynamicEntityKey.class)
@DenyIntrospection
public class AuditedEntity_a3t_2 extends AuditedEntity_a3t_1 {
  @IsProperty
  @MapTo("A3T_STR2")
  @Final
  @Title(
      value = "Str 2",
      desc = "[Str 2] at the time of the audited event."
  )
  private String a3t_str2;

  @IsProperty
  @InactiveAuditProperty
  @Title(
      value = "Str 1",
      desc = "Non-existing property."
  )
  private String a3t_str1;

  public String getA3t_str2() {
    return this.a3t_str2;
  }

  @Observable
  public AuditedEntity_a3t_2 setA3t_str2(final String a3t_str2) {
    this.a3t_str2 = a3t_str2;
    return this;
  }

  public String getA3t_str1() {
    return this.a3t_str1;
  }

  @Observable
  public AuditedEntity_a3t_2 setA3t_str1(final String a3t_str1) {
    this.a3t_str1 = a3t_str1;
    return this;
  }
}
