package ua.com.fielden.platform.gis;

import java.math.BigDecimal;

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
    private static double distanceInMetersBetween(final double lat1, final double lng1, final double lat2, final double lng2) {
	final double earthRadius = 3958.75;
	final double dLat = Math.toRadians(lat2 - lat1);
	final double dLng = Math.toRadians(lng2 - lng1);
	final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
	final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	final double dist = earthRadius * c;

	final int meterConversion = 1609;

	return dist * meterConversion;
    }

    /**
     * Calculates distance traveled between two sequential messages.
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static double calcDistance(final BigDecimal x1, final BigDecimal y1, final BigDecimal x2, final BigDecimal y2) {
	return calcDistance(x1.doubleValue(), y1.doubleValue(), x2.doubleValue(), y2.doubleValue());
    }

    /**
     * Calculates distance traveled between two sequential messages.
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static double calcDistance(final double x1, final double y1, final double x2, final double y2) {
	// return calcDistance0(x1, y1, x2, y2);
	return distanceInMetersBetween(x1, y1, x2, y2);
    }

//    /**
//     * Calculates distance traveled between two sequential messages.
//     *
//     * TODO WARNING : this calculation is incorrect!
//     *
//     * @param x1
//     * @param y1
//     * @param x2
//     * @param y2
//     * @return
//     */
//    private static double calcDistance0(final double x1, final double y1, final double x2, final double y2) {
//	final double dx = x1 - x2;
//	final double dy = y1 - y2;
//	return sqrt(dx * dx + dy * dy) * PI / 180 * 6371000;
//    }

    public static void main(final String[] args) {
	// довжина Яна Жижки -- має бути 87 метрів
	System.out.println(distanceInMetersBetween(49.844157, 24.028371, 49.844209, 24.029567));
	System.out.println(calcDistance(49.844157, 24.028371, 49.844209, 24.029567));
    }
}
