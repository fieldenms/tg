package ua.com.fielden.platform.sample.domain;

import java.lang.String;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import ua.com.fielden.platform.audit.AbstractAuditEntity;
import ua.com.fielden.platform.audit.AuditFor;
import ua.com.fielden.platform.entity.annotation.CompanionIsGenerated;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.types.Money;

@AuditFor(TgVehicle.class)
@MapEntityTo
@CompanionIsGenerated
public class TgVehicle_a3t_1 extends AbstractAuditEntity<TgVehicle> {
  @CompositeKeyMember(2)
  @MapTo
  @Required
  @Final
  @Title(
      value = "Tg Vehicle",
      desc = "Tg Vehicle entity"
  )
  @SkipEntityExistsValidation
  @IsProperty
  private TgVehicle auditedEntity;

  @Title(
      value = "Changed Properties",
      desc = "Properties changed as part of an audit event."
  )
  @IsProperty(TgVehicle_a3t_1_Prop.class)
  private final Set<TgVehicle_a3t_1_Prop> changedProps = new HashSet<>();

  @MapTo("A3T_KEY")
  @Final
  @IsProperty
  private String a3t_key;

  @MapTo("A3T_INITDATE")
  @Final
  @IsProperty
  private Date a3t_initDate;

  @MapTo("A3T_REPLACEDBY")
  @Final
  @SkipEntityExistsValidation
  @IsProperty
  private TgVehicle a3t_replacedBy;

  @MapTo("A3T_STATION")
  @Final
  @SkipEntityExistsValidation
  @IsProperty
  private TgOrgUnit5 a3t_station;

  @MapTo("A3T_MODEL")
  @Final
  @SkipEntityExistsValidation
  @IsProperty
  private TgVehicleModel a3t_model;

  @MapTo("A3T_PRICE")
  @Final
  @IsProperty
  private Money a3t_price;

  @MapTo("A3T_PURCHASEPRICE")
  @Final
  @IsProperty
  private Money a3t_purchasePrice;

  @MapTo("A3T_ACTIVE")
  @Final
  @IsProperty
  private boolean a3t_active;

  @MapTo("A3T_LEASED")
  @Final
  @IsProperty
  private boolean a3t_leased;

  @MapTo("A3T_LASTMETERREADING")
  @Final
  @IsProperty
  private BigDecimal a3t_lastMeterReading;

  @MapTo("A3T_DESC")
  @Final
  @IsProperty
  private String a3t_desc;

  public TgVehicle getAuditedEntity() {
    return this.auditedEntity;
  }

  @Observable
  public TgVehicle_a3t_1 setAuditedEntity(final TgVehicle auditedEntity) {
    this.auditedEntity = auditedEntity;
    return this;
  }

  public Set<TgVehicle_a3t_1_Prop> getChangedProps() {
    return Collections.unmodifiableSet(this.changedProps);
  }

  @Observable
  public TgVehicle_a3t_1 setChangedProps(final Set<TgVehicle_a3t_1_Prop> changedProps) {
    this.changedProps.clear();
    this.changedProps.addAll(changedProps);
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

  public Date getA3t_initDate() {
    return this.a3t_initDate;
  }

  @Observable
  public TgVehicle_a3t_1 setA3t_initDate(final Date a3t_initDate) {
    this.a3t_initDate = a3t_initDate;
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

  public boolean isA3t_active() {
    return this.a3t_active;
  }

  @Observable
  public TgVehicle_a3t_1 setA3t_active(final boolean a3t_active) {
    this.a3t_active = a3t_active;
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

  public BigDecimal getA3t_lastMeterReading() {
    return this.a3t_lastMeterReading;
  }

  @Observable
  public TgVehicle_a3t_1 setA3t_lastMeterReading(final BigDecimal a3t_lastMeterReading) {
    this.a3t_lastMeterReading = a3t_lastMeterReading;
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
}
