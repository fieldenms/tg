package ua.com.fielden.platform.gis.gps.actors;

public class AvlGpsElement {

    public static final int BASE_ELEMENT_LENGTH = 4 + 4 + 2 + 2 + 1 + 2;

    private static final int PRECISION = 10_000_000;

    public final int longitude;
    public final int latitude;
    private final short altitude;
    private final short angle;
    private final byte satellites;
    private final short speed;

    public AvlGpsElement(final int longitude, final int latitude, final short altitude, final short angle, final byte satellites, final short speed) {
	this.longitude = longitude;
	this.latitude = latitude;
	this.altitude = altitude;
	this.angle = angle;
	this.satellites = satellites;
	this.speed = speed;
    }

    public double getLongitude() {
	return (double) longitude / PRECISION;
    }

    public double getLatitude() {
	return (double) latitude / PRECISION;
    }

    public short getAltitude() {
	return altitude;
    }

    public short getAngle() {
	return angle;
    }

    public byte getSatellites() {
	return satellites;
    }

    public short getSpeed() {
	return speed;
    }

}
