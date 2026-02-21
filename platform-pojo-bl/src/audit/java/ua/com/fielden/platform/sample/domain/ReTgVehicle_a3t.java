// Generation timestamp: 2026-01-12 16:58:42 EET
package ua.com.fielden.platform.sample.domain;

import java.lang.String;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.audit.AbstractSynAuditEntity;
import ua.com.fielden.platform.audit.annotations.AuditFor;
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
import ua.com.fielden.platform.types.Money;

@AuditFor(TgVehicle.class)
@EntityTitle("Tg Vehicle Audit")
@SkipVerification
@SkipEntityRegistration
@CompanionIsGenerated
@KeyType(DynamicEntityKey.class)
public class ReTgVehicle_a3t extends AbstractSynAuditEntity<TgVehicle> {
  protected static List<EntityResultQueryModel<ReTgVehicle_a3t>> models_;

  @CompositeKeyMember(1)
  @Title(
      value = "Tg Vehicle",
      desc = "The audited Tg Vehicle."
  )
  @DenyIntrospection
  @IsProperty
  private TgVehicle auditedEntity;

  @Title(
      value = "Changed Properties",
      desc = "Properties changed as part of the audited event."
  )
  @DenyIntrospection
  @IsProperty(ReTgVehicle_a3t_Prop.class)
  private final Set<ReTgVehicle_a3t_Prop> changedProps = new HashSet<>();

  @Title(
      value = "Changed Properties",
      desc = "Properties changed as part of the audited event."
  )
  @CritOnly(CritOnly.Type.MULTI)
  @DenyIntrospection
  @IsProperty(ReTgVehicle_a3t.class)
  private PropertyDescriptor<ReTgVehicle_a3t> changedPropsCrit;

  @Title(
      value = "Active",
      desc = "[Active] at the time of the audited event."
  )
  @IsProperty
  private boolean a3t_active;

  @Title(
      value = "Description",
      desc = "[Description] at the time of the audited event."
  )
  @IsProperty
  private String a3t_desc;

  @Title(
      value = "Init Date",
      desc = "[Init Date] at the time of the audited event."
  )
  @IsProperty
  private Date a3t_initDate;

  @Title(
      value = "Key",
      desc = "[Key] at the time of the audited event."
  )
  @IsProperty
  private String a3t_key;

  @Title(
      value = "Last meter reading",
      desc = "[Last meter reading] at the time of the audited event."
  )
  @IsProperty
  private BigDecimal a3t_lastMeterReading;

  @Title(
      value = "Leased?",
      desc = "[Leased?] at the time of the audited event."
  )
  @IsProperty
  private boolean a3t_leased;

  @Title(
      value = "Model",
      desc = "[Model] at the time of the audited event."
  )
  @IsProperty
  private TgVehicleModel a3t_model;

  @Title(
      value = "Price",
      desc = "[Price] at the time of the audited event."
  )
  @IsProperty
  private Money a3t_price;

  @Title(
      value = "Purchase Price",
      desc = "[Purchase Price] at the time of the audited event."
  )
  @IsProperty
  private Money a3t_purchasePrice;

  @Title(
      value = "Tg Vehicle",
      desc = "[Tg Vehicle] at the time of the audited event."
  )
  @IsProperty
  private TgVehicle a3t_replacedBy;

  @Title(
      value = "Tg Org Unit5",
      desc = "[Tg Org Unit5] at the time of the audited event."
  )
  @IsProperty
  private TgOrgUnit5 a3t_station;

  public TgVehicle getAuditedEntity() {
    return this.auditedEntity;
  }

  @Observable
  public ReTgVehicle_a3t setAuditedEntity(final TgVehicle auditedEntity) {
    this.auditedEntity = auditedEntity;
    return this;
  }

  public Set<ReTgVehicle_a3t_Prop> getChangedProps() {
    return Collections.unmodifiableSet(this.changedProps);
  }

  @Observable
  public ReTgVehicle_a3t setChangedProps(final Set<ReTgVehicle_a3t_Prop> changedProps) {
    this.changedProps.clear();
    this.changedProps.addAll(changedProps);
    return this;
  }

  public PropertyDescriptor<ReTgVehicle_a3t> getChangedPropsCrit() {
    return this.changedPropsCrit;
  }

  @Observable
  public ReTgVehicle_a3t setChangedPropsCrit(
      final PropertyDescriptor<ReTgVehicle_a3t> changedPropsCrit) {
    this.changedPropsCrit = changedPropsCrit;
    return this;
  }

  public boolean isA3t_active() {
    return this.a3t_active;
  }

  @Observable
  public ReTgVehicle_a3t setA3t_active(final boolean a3t_active) {
    this.a3t_active = a3t_active;
    return this;
  }

  public String getA3t_desc() {
    return this.a3t_desc;
  }

  @Observable
  public ReTgVehicle_a3t setA3t_desc(final String a3t_desc) {
    this.a3t_desc = a3t_desc;
    return this;
  }

  public Date getA3t_initDate() {
    return this.a3t_initDate;
  }

  @Observable
  public ReTgVehicle_a3t setA3t_initDate(final Date a3t_initDate) {
    this.a3t_initDate = a3t_initDate;
    return this;
  }

  public String getA3t_key() {
    return this.a3t_key;
  }

  @Observable
  public ReTgVehicle_a3t setA3t_key(final String a3t_key) {
    this.a3t_key = a3t_key;
    return this;
  }

  public BigDecimal getA3t_lastMeterReading() {
    return this.a3t_lastMeterReading;
  }

  @Observable
  public ReTgVehicle_a3t setA3t_lastMeterReading(final BigDecimal a3t_lastMeterReading) {
    this.a3t_lastMeterReading = a3t_lastMeterReading;
    return this;
  }

  public boolean isA3t_leased() {
    return this.a3t_leased;
  }

  @Observable
  public ReTgVehicle_a3t setA3t_leased(final boolean a3t_leased) {
    this.a3t_leased = a3t_leased;
    return this;
  }

  public TgVehicleModel getA3t_model() {
    return this.a3t_model;
  }

  @Observable
  public ReTgVehicle_a3t setA3t_model(final TgVehicleModel a3t_model) {
    this.a3t_model = a3t_model;
    return this;
  }

  public Money getA3t_price() {
    return this.a3t_price;
  }

  @Observable
  public ReTgVehicle_a3t setA3t_price(final Money a3t_price) {
    this.a3t_price = a3t_price;
    return this;
  }

  public Money getA3t_purchasePrice() {
    return this.a3t_purchasePrice;
  }

  @Observable
  public ReTgVehicle_a3t setA3t_purchasePrice(final Money a3t_purchasePrice) {
    this.a3t_purchasePrice = a3t_purchasePrice;
    return this;
  }

  public TgVehicle getA3t_replacedBy() {
    return this.a3t_replacedBy;
  }

  @Observable
  public ReTgVehicle_a3t setA3t_replacedBy(final TgVehicle a3t_replacedBy) {
    this.a3t_replacedBy = a3t_replacedBy;
    return this;
  }

  public TgOrgUnit5 getA3t_station() {
    return this.a3t_station;
  }

  @Observable
  public ReTgVehicle_a3t setA3t_station(final TgOrgUnit5 a3t_station) {
    this.a3t_station = a3t_station;
    return this;
  }
}
