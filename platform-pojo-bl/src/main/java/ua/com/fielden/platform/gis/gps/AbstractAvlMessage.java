package ua.com.fielden.platform.gis.gps;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.Ignore;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.TransactionEntity;

/**
 * A common base entity for GPS message used for GPS GIS systems server and UI logic.
 *
 * @author TG Team
 *
 */
@EntityTitle(value = "GPS повідомлення", desc = "Повідомлення з GPS модуля")
@KeyTitle(value = "GPS повідомлення", desc = "Повідомлення з GPS модуля")
@KeyType(DynamicEntityKey.class)
@MapEntityTo("MESSAGES")
// TODO do not forget to provide companion object in its descendants -- @CompanionObject(IMessage.class)
@TransactionEntity("packetReceived")
public abstract class AbstractAvlMessage extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;
    // TODO public static final String MACHINE_PROP_ALIAS = "machineRouteDriver.machine";

    @IsProperty
    @MapTo
    @Title(value = "GPS час", desc = "Час, коли було згенеровано повідомлення")
    @CompositeKeyMember(2)
    private Date gpsTime;

    @IsProperty
    @MapTo
    @Title(value = "X-координата", desc = "Значення довготи")
    private BigDecimal x;

    @IsProperty
    @MapTo
    @Title(value = "Y-координата", desc = "Значення широти")
    private BigDecimal y;

    @IsProperty
    @MapTo
    @Title(value = "Кут", desc = "Кут повороту машини по відношенню до півночі.")
    private Integer vectorAngle;

    @IsProperty
    @MapTo
    @Title(value = "Швидкість", desc = "Точкова швидкість руху машину")
    private Integer vectorSpeed;

    @IsProperty
    @MapTo
    @Title(value = "Висота", desc = "Висота над рівнем моря.")
    private Integer altitude;

    @IsProperty
    @MapTo
    @Title(value = "К-сть супутників", desc = "Кількість супутників, видимих у момент генерування повідомлення.")
    private Integer visibleSattelites;

    @IsProperty
    @MapTo
    @Title(value = "Запалення?", desc = "Вказує, чи двигун працював у момент генерування повідомлення.")
    private boolean din1;

    @IsProperty
    @MapTo
    @Title(value = "Вольтаж БЖ", desc = "Вольтаж блоку живлення.")
    private BigDecimal powerSupplyVoltage;

    @IsProperty
    @MapTo
    @Title(value = "Вольтаж акумулятора", desc = "Вольтаж акумулятора.")
    private BigDecimal batteryVoltage;

    @IsProperty
    @MapTo
    @Title(value = "GPS напруга?", desc = "Вказує, чи GPS модуль живився від зовнішнього джерела (не від внутрішнього акумулятора) у момент генерування повідомлення.")
    private boolean gpsPower;

    @IsProperty
    @MapTo("distance_") // TODO
    @Readonly
    @Required
    @Title(value = "Відстань", desc = "Відстань в метрах, яку було пройдено машиною з моменту отримання попереднього повідомлення.")
    private BigDecimal travelledDistance;

    @IsProperty
    @Ignore
    @MapTo("packet_")
    @Title(value = "Packet received date")
    private Date packetReceived;

    @IsProperty
    @MapTo
    @Ignore
    @Invisible
    private Integer status;

    @Observable
    public AbstractAvlMessage setStatus(final Integer status) {
	this.status = status;
	return this;
    }

    public Integer getStatus() {
	return status;
    }

    @Observable
    public AbstractAvlMessage setPacketReceived(final Date packetReceived) {
	this.packetReceived = packetReceived;
	return this;
    }

    public Date getPacketReceived() {
	return packetReceived;
    }

    @Observable
    public AbstractAvlMessage setTravelledDistance(final BigDecimal travelledDistance) {
	this.travelledDistance = travelledDistance;
	return this;
    }

    public BigDecimal getTravelledDistance() {
	return travelledDistance;
    }

    @Observable
    public AbstractAvlMessage setGpsPower(final boolean gpsPower) {
	this.gpsPower = gpsPower;
	return this;
    }

    public boolean getGpsPower() {
	return gpsPower;
    }

    @Observable
    public AbstractAvlMessage setBatteryVoltage(final BigDecimal batteryVoltage) {
	this.batteryVoltage = batteryVoltage;
	return this;
    }

    public BigDecimal getBatteryVoltage() {
	return batteryVoltage;
    }

    @Observable
    public AbstractAvlMessage setPowerSupplyVoltage(final BigDecimal powerSupplyVoltage) {
	this.powerSupplyVoltage = powerSupplyVoltage;
	return this;
    }

    public BigDecimal getPowerSupplyVoltage() {
	return powerSupplyVoltage;
    }

    @Observable
    public AbstractAvlMessage setDin1(final boolean din1) {
	this.din1 = din1;
	return this;
    }

    public boolean getDin1() {
	return din1;
    }

    @Observable
    public AbstractAvlMessage setVisibleSattelites(final Integer visibleSattelites) {
	this.visibleSattelites = visibleSattelites;
	return this;
    }

    public Integer getVisibleSattelites() {
	return visibleSattelites;
    }


    @Observable
    public AbstractAvlMessage setAltitude(final Integer altitude) {
	this.altitude = altitude;
	return this;
    }

    public Integer getAltitude() {
	return altitude;
    }


    @Observable
    public AbstractAvlMessage setVectorSpeed(final Integer vectorSpeed) {
	this.vectorSpeed = vectorSpeed;
	return this;
    }

    public Integer getVectorSpeed() {
	return vectorSpeed;
    }


    @Observable
    public AbstractAvlMessage setVectorAngle(final Integer vectorAngle) {
	this.vectorAngle = vectorAngle;
	return this;
    }

    public Integer getVectorAngle() {
	return vectorAngle;
    }

    @Observable
    public AbstractAvlMessage setGpsTime(final Date gpsTime) {
	this.gpsTime = gpsTime;
	return this;
    }

    public Date getGpsTime() {
	return gpsTime;
    }

    @Observable
    public AbstractAvlMessage setY(final BigDecimal y) {
	this.y = y;
	return this;
    }

    public BigDecimal getY() {
	return y;
    }

    @Observable
    public AbstractAvlMessage setX(final BigDecimal x) {
	this.x = x;
	return this;
    }

    public BigDecimal getX() {
	return x;
    }
}