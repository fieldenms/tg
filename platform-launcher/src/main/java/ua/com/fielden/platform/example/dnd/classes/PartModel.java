package ua.com.fielden.platform.example.dnd.classes;

import java.awt.Color;
import java.awt.Stroke;

import ua.com.fielden.platform.pmodels.IPaintable;
import edu.umd.cs.piccolo.nodes.PPath;

public class PartModel extends PPath implements IPaintable {

    /**
     *
     */
    private static final long serialVersionUID = 5066410384860797635L;
    private Color originColor, originStrokeColor;
    private static Stroke originStroke;

    public PartModel() {
        super();
    }

    @Override
    public void fill(final Color color) {
        if (color == null) {
            setPaint(originColor);
        } else {
            setPaint(color);
        }
    }

    @Override
    public void stroke(final Color color) {
        if (color == null) {
            setStrokePaint(originStrokeColor);
        } else {
            setStrokePaint(color);
        }
    }

    @Override
    public void stroke(final Stroke stroke) {
        if (stroke == null) {
            setStroke(originStroke);
        } else {
            setStroke(stroke);
        }
    }

    public Color getOriginColor() {
        return originColor;
    }

    public void setOriginColor(final Color originColor) {
        this.originColor = originColor;
    }

    public Color getOriginStrokeColor() {
        return originStrokeColor;
    }

    public void setOriginStrokeColor(final Color originStrokeColor) {
        this.originStrokeColor = originStrokeColor;
    }

    public Stroke getOriginStroke() {
        return originStroke;
    }

    public void setOriginStroke(final Stroke originStroke) {
        this.originStroke = originStroke;
    }
}
