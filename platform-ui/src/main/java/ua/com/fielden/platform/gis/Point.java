package ua.com.fielden.platform.gis;

import java.util.Date;

public class Point implements Comparable<Point> {
    private final Date timestamp, gpstime;
    private final int speed;
    private final double latitude, longitude;

    public Point(final Date timestamp, final Date gpstime, final int speed, final double latitude, final double longitude) {
	this.timestamp = timestamp;
	this.gpstime = gpstime;
	this.speed = speed;
	this.latitude = latitude;
	this.longitude = longitude;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Date getGpstime() {
        return gpstime;
    }

    public int getSpeed() {
        return speed;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public int compareTo(final Point o) {
	return new Long(getGpstime().getTime()).compareTo(new Long(o.getGpstime().getTime()));
    }

    @Override
    public String toString() {
	return "Point [timestamp=" + timestamp + ", gpstime=" + gpstime + ", speed=" + speed + ", latitude=" + latitude + ", longitude=" + longitude + "]";
    }
}
