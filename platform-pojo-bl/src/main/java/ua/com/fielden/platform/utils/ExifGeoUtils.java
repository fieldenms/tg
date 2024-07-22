package ua.com.fielden.platform.utils;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * Utilities for processing geolocation information embedded within images according to the Exif standard.
 */
public final class ExifGeoUtils {

    public static Optional<Coordinates> getCoordinates(final File file) throws IOException {
        return getCoordinates(Imaging.getMetadata(file));
    }

    public static Optional<Coordinates> getCoordinates(final InputStream inputStream) throws IOException {
        return getCoordinates(Imaging.getMetadata(inputStream, null));
    }

    public static Optional<Coordinates> getCoordinates(final ImageMetadata metadata) throws ImagingException {
        return switch (metadata) {
            case JpegImageMetadata it -> ofNullable(getCoordinates(it.getExif()));
            case TiffImageMetadata it -> ofNullable(getCoordinates(it));
            default -> empty();
        };
    }

    private static @Nullable Coordinates getCoordinates(final TiffImageMetadata metadata) throws ImagingException {
        return metadata.getGpsInfo() == null ? null : Coordinates.fromGpsInfo(metadata.getGpsInfo());
    }

    public record Coordinates(double latitude, double longitude) {
        public static Coordinates fromGpsInfo(final TiffImageMetadata.GpsInfo gpsInfo) throws ImagingException {
            return new Coordinates(gpsInfo.getLatitudeAsDegreesNorth(), gpsInfo.getLongitudeAsDegreesEast());
        }
    }

    private ExifGeoUtils() {}

}
