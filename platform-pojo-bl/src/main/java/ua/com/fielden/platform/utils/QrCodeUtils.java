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
    /// The image size will be exactly `width` x `height`.
    /// The larger the value of `border`, the less area will be allocated to the QR code.
    ///
    /// @param imageFormat the image format to use
    /// @param width       width of the image, in pixels
    /// @param height      height of the image, in pixels
    /// @param border      size of the border on each side
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
        if (border >= width || border >= height) {
            throw new InvalidArgumentException("Border must not be >= than width or height.");
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

        // The QR code is unlikely to perfectly fit the specified width and height.
        // It is more likely to occupy a slightly smaller area.
        // E.g., in a 512x512 image, a QR code with 53x53 modules will occupy 477 pixels (9 pixels per each module).
        // The remaining pixels will be integrated into the border area.

        final int remWidth = Integer.remainderUnsigned(width - border*2, qr.size);
        final int remHeight = Integer.remainderUnsigned(height - border*2, qr.size);

        // The area occupied by the QR code.
        int qrWidth = width - border*2 - remWidth;
        int qrHeight = height - border*2 - remHeight;

        int realBorderX = border + (remWidth / 2);
        int realBorderY = border + (remHeight / 2);

        // Number of pixels per module.
        int widthScale = qrWidth / qr.size;
        int heightScale = qrHeight / qr.size;

        // Readjust the image size if the specified size is too small to contain the QR code.
        // 4 pixels per one QR code module should be enough.

        if (widthScale == 0) {
            LOGGER.warn("Specified image width [%s] is too small for QR code with size [%s]. Using a default width.".formatted(width, qr.size));
            widthScale = 4;
            qrWidth = width = qr.size * widthScale;
            realBorderX = 0;
        }

        if (heightScale == 0) {
            LOGGER.warn("Specified image height [%s] is too small for QR code with size [%s]. Using a default height.".formatted(height, qr.size));
            heightScale = 4;
            qrHeight = height = qr.size * heightScale;
            realBorderY = 0;
        }

        final var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Fill with light colour.
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, lightColourRgb);
            }
        }
        // Draw the QR code.
        for (int y = 0; y < qrHeight; y++) {
            for (int x = 0; x < qrWidth; x++) {
                final boolean color = qr.getModule(x / widthScale, y / heightScale);
                image.setRGB(x + realBorderX, y + realBorderY, color ? darkColourRgb : lightColourRgb);
            }
        }

        return image;
    }

    private QrCodeUtils() {}

}
