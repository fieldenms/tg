package ua.com.fielden.platform.utils;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * Utilities for processing geolocation information embedded within images according to the Exif standard.
 */
public final class ExifGeoUtils {

    /**
     * Extracts latitude and longitude from Exif data embedded within a file.
     * If a file is not an image or has no geolocation metadata, empty optional is returned.
     */
    public static Optional<Coordinates> getCoordinates(final File file) throws IOException {
        if (ImageFormats.UNKNOWN == Imaging.guessFormat(file)) {
            return empty();
        }
        return getCoordinates(Imaging.getMetadata(file));
    }

    // Reading from an input stream can be supported but requires a clever InputStream wrapper so that we can guess the
    // format first (like we do with a file). If we don't check the format and the stream's contents do not represent
    // an image, we will receive an IllegalArgumentException, whereas we'd like to return empty Optional instead.
    // If only Apache Commons Imaging used a specific exception type for such cases...

//    /**
//     * Extracts latitude and longitude from Exif data embedded within a file.
//     * If a file is not an image or has no geolocation metadata, empty optional is returned.
//     *
//     * @param inputStream  contents of a file
//     */
//    public static Optional<Coordinates> getCoordinates(final InputStream inputStream) throws IOException {
//        return getCoordinates(Imaging.getMetadata(inputStream, null));
//    }

    /**
     * Extracts latitude and longitude from image metadata.
     */
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
