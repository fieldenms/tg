package ua.com.fielden.platform.gis.gps.actors;

/**
 * Teltonica specific IO element codes.
 * 
 * @author TG Team
 *
 */
public enum AvlIoCodes {
    DIN1(1),
    VIRT_ODOMETER(199),
    POWER_SUPPLY_VOLT(66),
    BATTERY_VOLT(67),
    GPS_POWER(69);

    public final int id;
    AvlIoCodes(final int val) {
	this.id = val;
    }
}
