package ua.com.fielden.platform.utils;

import io.nayuki.qrcodegen.DataTooLongException;
import io.nayuki.qrcodegen.QrCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/// Utilities for working with QR codes.
///
public final class QrCodeUtils {

    public static final String WHITE = "0xFFFFFF", BLACK = "0x000000";

    private static final int WHITE_RGB = 0xFFFFFF, BLACK_RGB = 0x000000;

    /// Creates a QR code image for the content in `input`.
    ///
    /// @param imageFormat the image format to use
    /// @param width       width of the image, in pixels
    /// @param height      height of the image, in pixels
    /// @param border      size of the border around the image, in pixels
    /// @param lightColour hex colour for the light squares (e.g., "0xFFFFFF")
    /// @param darkColour  hex colour for the dark squares (e.g., "0x000000")
    /// @return            bytes that constitute the image in the specified format
    ///
    /// @throws InputTooLongException if the input is too long to fit in a QR code
    ///
    public static byte[] qrCodeImage(
            final CharSequence input,
            final ImageFormat imageFormat,
            final int width,
            final int height,
            final int border,
            final String lightColour,
            final String darkColour)
    {
        final QrCode qr;
        try {
            qr = QrCode.encodeText(input, QrCode.Ecc.HIGH);
        } catch (final DataTooLongException ex) {
            throw new InputTooLongException(ex);
        }
        final var img = toImage(qr, width, height, border, lightColour, darkColour);
        final var formatName = switch (imageFormat) {
            case PNG -> "png";
        };
        final var baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, formatName, baos);
        } catch (final IOException ex) {
            throw new RuntimeException("Failed to create an image from a QR code.", ex);
        }
        return baos.toByteArray();
    }

    public enum ImageFormat {
        PNG
    }

    public static class InputTooLongException extends InvalidArgumentException {

        private InputTooLongException(final Throwable cause) {
            super("The input is too long to fit in a QR code.", cause);
        }

    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Implementation
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private static final Logger LOGGER = LogManager.getLogger();

    private static BufferedImage toImage(
            final QrCode qr,
            int width,
            int height,
            final int border,
            final String lightColour,
            final String darkColour)
    {
        if (width < 0) {
            throw new InvalidArgumentException("Width must be >= 0, but was %s.".formatted(width));
        }
        if (height < 0) {
            throw new InvalidArgumentException("Height must be >= 0, but was %s.".formatted(height));
        }
        if (border < 0) {
            throw new InvalidArgumentException("Border must be >= 0, but was %s.".formatted(border));
        }

        int lightColourRgb;
        try {
            lightColourRgb = Integer.parseInt(lightColour, 2, lightColour.length(), 16);
        } catch (final NumberFormatException ex) {
            lightColourRgb = WHITE_RGB;
            LOGGER.warn(() -> "Invalid colour specified: [%s]. Using a default colour.".formatted(lightColour));
        }

        int darkColourRgb;
        try {
            darkColourRgb = Integer.parseInt(darkColour, 2, darkColour.length(), 16);
        } catch (final NumberFormatException ex) {
            darkColourRgb = BLACK_RGB;
            LOGGER.warn(() -> "Invalid colour specified: [%s]. Using a default colour.".formatted(darkColour));
        }

        final int qrSide = qr.size;
        int widthScale = width / qrSide;
        int heightScale = height / qrSide;

        // Readjust the image size if the specified size is too small to contain the QR code.
        // 4 pixels per one QR code module should be enough.

        if (widthScale == 0) {
            LOGGER.warn("Specified image width [%s] is too small for QR code with size [%s]. Using a default width.".formatted(width, qrSide));
            widthScale = 4;
            width = qrSide * widthScale;
        }

        if (heightScale == 0) {
            LOGGER.warn("Specified image height [%s] is too small for QR code with size [%s]. Using a default height.".formatted(height, qrSide));
            heightScale = 4;
            height = qrSide * heightScale;
        }

        final var image = new BufferedImage(width + border*2, height + border*2, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (x < border || x >= width + border || y < border || y >= height + border) {
                    image.setRGB(x, y, lightColourRgb);
                }
                else {
                    final boolean color = qr.getModule((x - border) / widthScale, (y - border) / heightScale);
                    image.setRGB(x, y, color ? darkColourRgb : lightColourRgb);
                }
            }
        }
        return image;
    }

    private QrCodeUtils() {}

}
