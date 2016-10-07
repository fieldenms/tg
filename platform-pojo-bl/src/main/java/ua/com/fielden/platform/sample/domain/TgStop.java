package ua.com.fielden.platform.sample.domain;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.ResultOnly;
import ua.com.fielden.platform.entity.annotation.Title;

@EntityTitle(value = "Зупинка", desc = "Зупинка машини")
@KeyType(String.class)
@KeyTitle(value = "Зупинка", desc = "Зупинка машини")
@CompanionObject(ITgStop.class)
public class TgStop extends AbstractEntity<String> {
    private static final long serialVersionUID = -1855554181161145321L;

    // filtering
    @IsProperty
    @MapTo
    @Title(value = "Машина", desc = "Машина, для котрої здійснюється пошук зупинок")
    @CritOnly
    private TgMachine machine;

    @IsProperty
    @MapTo
    @Title(value = "Машина", desc = "Машина, котра здійснила зупинку")
    @ResultOnly
    private TgMachine machineResult;

    @IsProperty
    @MapTo
    @Title(value = "Організаційний підрозділ", desc = "Організаційний підрозділ, для машин котрого здійснюється пошук зупинок")
    @CritOnly
    private TgOrgUnit orgUnit;

    @IsProperty
    @MapTo
    @Title(value = "Організаційний підрозділ", desc = "Організаційний підрозділ, до котрого належить машина, що здійснила зупинку")
    @ResultOnly
    private TgOrgUnit orgUnitResult;

    @IsProperty
    @MapTo
    @Title(value = "GPS час", desc = "Орієнтовний час, для котрого здійснюється пошук зупинок")
    @CritOnly
    private Date gpsTime;

    // Resultant and algorithmic filtering
    @IsProperty
    @MapTo
    @Title(value = "Час простою", desc = "Кількість хвилин, коли машина була в простої")
    @CritOnly
    private BigDecimal durationInMinutes;

    @IsProperty
    @MapTo
    @Title(value = "Час простою", desc = "Кількість хвилин, коли машина була в простої")
    @ResultOnly
    private BigDecimal durationInMinutesResult;

    @IsProperty
    @MapTo
    @Title(value = "Час простою", desc = "Період часу, коли машина була в простої")
    @ResultOnly
    private String periodString;

    @IsProperty
    @MapTo
    @Title(value = "Нічна стоянка?", desc = "Враховувати нічні стоянки при пошуку зупинки?")
    @CritOnly
    private boolean nightStop;

    @IsProperty
    @MapTo
    @Title(value = "Нічна стоянка?", desc = "Вказує, чи зупинка є нічною стоянкою")
    @ResultOnly
    private boolean nightStopResult;

    @IsProperty
    @MapTo
    @Title(value = "Відстань", desc = "Відстань в метрах, яку проїхала машина під час зупинки")
    @ResultOnly
    private BigDecimal distance;

    @IsProperty
    @MapTo
    @Title(value = "Радіус зупинки", desc = "Орієнтовний радіус області, де знаходилась на зупинці машина")
    @ResultOnly
    private BigDecimal radius;

    @IsProperty
    @MapTo
    @Title(value = "Початок зупинки", desc = "Орієнтовний час початку зупинки")
    @ResultOnly
    private Date stopTimeFrom;

    @IsProperty
    @MapTo
    @Title(value = "Початок зупинки", desc = "Орієнтовний час початку зупинки")
    @ResultOnly
    private String stopTimeFromString;

    @IsProperty
    @MapTo
    @Title(value = "Кінець зупинки", desc = "Орієнтовний час закінчення зупинки")
    @ResultOnly
    private Date stopTimeTo;

    @IsProperty
    @MapTo
    @Title(value = "Кінець зупинки", desc = "Орієнтовний час закінчення зупинки")
    @ResultOnly
    private String stopTimeToString;

    @IsProperty
    @MapTo
    @Title(value = "X-координата центра зупинки", desc = "Значення довготи для географічного центра зупинки")
    @ResultOnly
    private BigDecimal baryCentreX;

    @IsProperty
    @MapTo
    @Title(value = "Y-координата центра зупинки", desc = "Значення широти для географічного центра зупинки")
    @ResultOnly
    private BigDecimal baryCentreY;

    @IsProperty(TgMessage.class)
    @Title(value = "Повідомлення", desc = "Повідомлення, згенеровані під час зупинки")
    private Set<TgMessage> messages = new LinkedHashSet<TgMessage>();

    @IsProperty
    @MapTo
    @Title(value = "Повідомлення -- опис", desc = "")
    @ResultOnly
    private String MessagesString;

    // Stop contract properties
    @IsProperty
    @MapTo
    @Title(value = "Пороговий радіус", desc = "Найбільш можливе значення радіуса зупинки в метрах. Чим більше це значення, тим більші по розміру зупинки будуть сформовані")
    @CritOnly(Type.SINGLE)
    @Required
    private BigDecimal radiusThreshould;

    @IsProperty
    @MapTo
    @Title(value = "Порогова швидкість", desc = "Найбільш можливе значення швидкості в повідомленнях зупинки (км / год). Чим більше це значення, тим точніше буде показано трек під час зупинки, але довше виконуватиметься запит")
    @CritOnly(Type.SINGLE)
    @Required
    private BigDecimal speedThreshould;

    @Observable
    public TgStop setRadiusThreshould(final BigDecimal radiusThreshould) {
	this.radiusThreshould = radiusThreshould;
	return this;
    }

    public BigDecimal getRadiusThreshould() {
	return radiusThreshould;
    }

    @Observable
    public TgStop setSpeedThreshould(final BigDecimal speedThreshould) {
	this.speedThreshould = speedThreshould;
	return this;
    }

    public BigDecimal getSpeedThreshould() {
	return speedThreshould;
    }

    @Observable
    public TgStop setStopTimeTo(final Date stopTimeTo) {
	this.stopTimeTo = stopTimeTo;
	return this;
    }

    public Date getStopTimeTo() {
	return stopTimeTo;
    }

    @Observable
    public TgStop setStopTimeFrom(final Date stopTimeFrom) {
	this.stopTimeFrom = stopTimeFrom;
	return this;
    }

    public Date getStopTimeFrom() {
	return stopTimeFrom;
    }

    @Observable
    public TgStop setOrgUnit(final TgOrgUnit orgUnit) {
	this.orgUnit = orgUnit;
	return this;
    }

    public TgOrgUnit getOrgUnit() {
	return orgUnit;
    }

    @Observable
    public TgStop setRadius(final BigDecimal radius) {
	this.radius = radius;
	return this;
    }

    public BigDecimal getRadius() {
	return radius;
    }

    @Observable
    public TgStop setMessagesString(final String MessagesString) {
	this.MessagesString = MessagesString;
	return this;
    }

    public String getMessagesString() {
	return MessagesString;
    }

    @Observable
    public TgStop setDistance(final BigDecimal distance) {
	this.distance = distance;
	return this;
    }

    public BigDecimal getDistance() {
	return distance;
    }

    @Observable
    public TgStop setDurationInMinutes(final BigDecimal durationInMinutes) {
	this.durationInMinutes = durationInMinutes;
	return this;
    }

    public BigDecimal getDurationInMinutes() {
	return durationInMinutes;
    }

    @Observable
    protected TgStop setMessages(final Set<TgMessage> messages) {
	this.messages.clear();
	this.messages.addAll(messages);
	return this;
    }

    public Set<TgMessage> getMessages() {
	return Collections.unmodifiableSet(messages);
    }

    @Observable
    public TgStop setMachine(final TgMachine machine) {
	this.machine = machine;
	return this;
    }

    public TgMachine getMachine() {
	return machine;
    }

    @Observable
    public TgStop setGpsTime(final Date gpsTime) {
	this.gpsTime = gpsTime;
	return this;
    }

    public Date getGpsTime() {
	return gpsTime;
    }

    @Observable
    public TgStop setMachineResult(final TgMachine machineResult) {
	this.machineResult = machineResult;
	return this;
    }

    public TgMachine getMachineResult() {
	return machineResult;
    }

    @Observable
    public TgStop setOrgUnitResult(final TgOrgUnit orgUnitResult) {
	this.orgUnitResult = orgUnitResult;
	return this;
    }

    public TgOrgUnit getOrgUnitResult() {
	return orgUnitResult;
    }

    @Observable
    public TgStop setDurationInMinutesResult(final BigDecimal durationInMinutesResult) {
	this.durationInMinutesResult = durationInMinutesResult;
	return this;
    }

    public BigDecimal getDurationInMinutesResult() {
	return durationInMinutesResult;
    }

    @Observable
    public TgStop setBaryCentreX(final BigDecimal baryCentreX) {
	this.baryCentreX = baryCentreX;
	return this;
    }

    public BigDecimal getBaryCentreX() {
	return baryCentreX;
    }

    @Observable
    public TgStop setBaryCentreY(final BigDecimal baryCentreY) {
	this.baryCentreY = baryCentreY;
	return this;
    }

    public BigDecimal getBaryCentreY() {
	return baryCentreY;
    }

    @Observable
    public TgStop setPeriodString(final String periodString) {
	this.periodString = periodString;
	return this;
    }

    public String getPeriodString() {
	return periodString;
    }

    @Observable
    public TgStop setNightStopResult(final boolean nightStopResult) {
	this.nightStopResult = nightStopResult;
	return this;
    }

    public boolean getNightStopResult() {
	return nightStopResult;
    }

    @Observable
    public TgStop setNightStop(final boolean nightStop) {
	this.nightStop = nightStop;
	return this;
    }

    public Boolean getNightStop() {
	return nightStop;
    }

    @Observable
    public TgStop setStopTimeFromString(final String stopTimeFromString) {
	this.stopTimeFromString = stopTimeFromString;
	return this;
    }

    public String getStopTimeFromString() {
	return stopTimeFromString;
    }

    @Observable
    public TgStop setStopTimeToString(final String stopTimeToString) {
	this.stopTimeToString = stopTimeToString;
	return this;
    }

    public String getStopTimeToString() {
	return stopTimeToString;
    }
}