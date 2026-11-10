package ld.e2e.support;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/** Petites images valides générées en mémoire (pas de binaire dans le dépôt). */
public final class Images {

    private Images() {
    }

    public static byte[] png(int rgb) {
        BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                image.setRGB(x, y, rgb);
            }
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static byte[] red() {
        return png(0xFF0000);
    }

    public static byte[] blue() {
        return png(0x0000FF);
    }
}