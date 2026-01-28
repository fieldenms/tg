// Generation timestamp: 2026-01-16 10:46:36 EET
package ua.com.fielden.platform.sample.domain;

import java.lang.String;
import java.math.BigDecimal;
import java.util.Date;
import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.annotations.metamodel.WithoutMetaModel;
import ua.com.fielden.platform.audit.AbstractAuditEntity;
import ua.com.fielden.platform.audit.AuditFor;
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
import ua.com.fielden.platform.types.Money;

@AuditFor(
    value = TgVehicle.class,
    version = 1
)
@MapEntityTo
@CompanionIsGenerated
@EntityTitle("Tg Vehicle Audit 1")
@SkipVerification
@SkipEntityRegistration
@WithoutMetaModel
@KeyType(DynamicEntityKey.class)
@DenyIntrospection
public class TgVehicle_a3t_1 extends AbstractAuditEntity<TgVehicle> {
  @IsProperty
  @MapTo
  @Final
  @Title(
      value = "Active",
      desc = "[Active] at the time of the audited event."
  )
  private boolean a3t_active;

  @IsProperty
  @MapTo("A3T_DESC_")
  @Final
  @Title(
      value = "Description",
      desc = "[Description] at the time of the audited event."
  )
  private String a3t_desc;

  @IsProperty
  @MapTo
  @Final
  @Title(
      value = "Init Date",
      desc = "[Init Date] at the time of the audited event."
  )
  private Date a3t_initDate;

  @IsProperty
  @MapTo("A3T_KEY_")
  @Final
  @Title(
      value = "Key",
      desc = "[Key] at the time of the audited event."
  )
  private String a3t_key;

  @IsProperty(
      precision = 10,
      scale = 3
  )
  @MapTo
  @Final
  @Title(
      value = "Last meter reading",
      desc = "[Last meter reading] at the time of the audited event."
  )
  private BigDecimal a3t_lastMeterReading;

  @IsProperty
  @MapTo
  @Final
  @Title(
      value = "Leased?",
      desc = "[Leased?] at the time of the audited event."
  )
  private boolean a3t_leased;

  @IsProperty
  @MapTo
  @Final
  @Title(
      value = "Model",
      desc = "[Model] at the time of the audited event."
  )
  @SkipEntityExistsValidation
  private TgVehicleModel a3t_model;

  @IsProperty
  @MapTo
  @Final
  @Title(
      value = "Price",
      desc = "[Price] at the time of the audited event."
  )
  private Money a3t_price;

  @IsProperty
  @MapTo
  @Final
  @Title(
      value = "Purchase Price",
      desc = "[Purchase Price] at the time of the audited event."
  )
  private Money a3t_purchasePrice;

  @IsProperty
  @MapTo
  @Final
  @Title(
      value = "Tg Vehicle",
      desc = "[Tg Vehicle] at the time of the audited event."
  )
  @SkipEntityExistsValidation
  private TgVehicle a3t_replacedBy;

  @IsProperty
  @MapTo
  @Final
  @Title(
      value = "Tg Org Unit5",
      desc = "[Tg Org Unit5] at the time of the audited event."
  )
  @SkipEntityExistsValidation
  private TgOrgUnit5 a3t_station;

  @CompositeKeyMember(1)
  @MapTo
  @Required
  @Final
  @Title(
      value = "Tg Vehicle",
      desc = "The audited Tg Vehicle."
  )
  @SkipEntityExistsValidation
  @IsProperty
  private TgVehicle auditedEntity;

  public boolean isA3t_active() {
    return this.a3t_active;
  }

  @Observable
  public TgVehicle_a3t_1 setA3t_active(final boolean a3t_active) {
    this.a3t_active = a3t_active;
    return this;
  }

  public String getA3t_desc() {
    return this.a3t_desc;
  }

  @Observable
  public TgVehicle_a3t_1 setA3t_desc(final String a3t_desc) {
    this.a3t_desc = a3t_desc;
    return this;
  }

  public Date getA3t_initDate() {
    return this.a3t_initDate;
  }

  @Observable
  public TgVehicle_a3t_1 setA3t_initDate(final Date a3t_initDate) {
    this.a3t_initDate = a3t_initDate;
    return this;
  }

  public String getA3t_key() {
    return this.a3t_key;
  }

  @Observable
  public TgVehicle_a3t_1 setA3t_key(final String a3t_key) {
    this.a3t_key = a3t_key;
    return this;
  }

  public BigDecimal getA3t_lastMeterReading() {
    return this.a3t_lastMeterReading;
  }

  @Observable
  public TgVehicle_a3t_1 setA3t_lastMeterReading(final BigDecimal a3t_lastMeterReading) {
    this.a3t_lastMeterReading = a3t_lastMeterReading;
    return this;
  }

  public boolean isA3t_leased() {
    return this.a3t_leased;
  }

  @Observable
  public TgVehicle_a3t_1 setA3t_leased(final boolean a3t_leased) {
    this.a3t_leased = a3t_leased;
    return this;
  }

  public TgVehicleModel getA3t_model() {
    return this.a3t_model;
  }

  @Observable
  public TgVehicle_a3t_1 setA3t_model(final TgVehicleModel a3t_model) {
    this.a3t_model = a3t_model;
    return this;
  }

  public Money getA3t_price() {
    return this.a3t_price;
  }

  @Observable
  public TgVehicle_a3t_1 setA3t_price(final Money a3t_price) {
    this.a3t_price = a3t_price;
    return this;
  }

  public Money getA3t_purchasePrice() {
    return this.a3t_purchasePrice;
  }

  @Observable
  public TgVehicle_a3t_1 setA3t_purchasePrice(final Money a3t_purchasePrice) {
    this.a3t_purchasePrice = a3t_purchasePrice;
    return this;
  }

  public TgVehicle getA3t_replacedBy() {
    return this.a3t_replacedBy;
  }

  @Observable
  public TgVehicle_a3t_1 setA3t_replacedBy(final TgVehicle a3t_replacedBy) {
    this.a3t_replacedBy = a3t_replacedBy;
    return this;
  }

  public TgOrgUnit5 getA3t_station() {
    return this.a3t_station;
  }

  @Observable
  public TgVehicle_a3t_1 setA3t_station(final TgOrgUnit5 a3t_station) {
    this.a3t_station = a3t_station;
    return this;
  }

  public TgVehicle getAuditedEntity() {
    return this.auditedEntity;
  }

  @Observable
  public TgVehicle_a3t_1 setAuditedEntity(final TgVehicle auditedEntity) {
    this.auditedEntity = auditedEntity;
    return this;
  }
}
