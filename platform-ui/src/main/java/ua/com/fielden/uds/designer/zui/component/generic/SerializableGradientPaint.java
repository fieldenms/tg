/**
 * 
 */
package ua.com.fielden.uds.designer.zui.component.generic;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.io.Serializable;

/**
 * This is a paint, which can be serialized.
 * 
 * @author 01es
 * 
 */
public class SerializableGradientPaint implements Paint, Serializable {
    private static final long serialVersionUID = -5276082250417779238L;

    private float startPointX;
    private float startPointY;
    private Color startColor;
    private float endPointX;
    private float endPointY;
    private Color endColor;

    public SerializableGradientPaint() {
    }

    public SerializableGradientPaint(float x1, float y1, Color color1, float x2, float y2, Color color2) {
	startPointX = x1;
	startPointY = y1;
	startColor = color1;
	endPointX = x2;
	endPointY = y2;
	endColor = color2;
    }

    private Paint getGradientPaint() {
	return new GradientPaint(startPointX, startPointY, startColor, endPointX, endPointY, endColor);
    }

    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
	Paint paint = getGradientPaint();
	return paint.createContext(cm, deviceBounds, userBounds, xform, hints);
    }

    public int getTransparency() {
	Paint paint = getGradientPaint();
	return paint.getTransparency();
    }
}