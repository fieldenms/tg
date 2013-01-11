package ua.com.fielden.platform.javafx.gis;

/**
 * Contains useful map utils.
 * 
 * @author TG Team
 *
 */
public class MapUtils {
    
    /**
     * Calculates a distance between two "world" points.
     * 
     * @see http://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java
     * 
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public static double distanceInMetersBetween(final double lat1, final double lng1, final double lat2, final double lng2) {
	final double earthRadius = 3958.75;
	final double dLat = Math.toRadians(lat2 - lat1);
	final double dLng = Math.toRadians(lng2 - lng1);
	final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
	final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	final double dist = earthRadius * c;

	final int meterConversion = 1609;

	return dist * meterConversion;
    }

}
