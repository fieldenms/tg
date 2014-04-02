/**
 *
 */
package ua.com.fielden.platform.swing.review;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.SortOrder;

/**
 * Component for displaying {@link PropertyOrderingModel}. Instances of this class also serves as controllers ({@link IRepaintNotifier}s) which repaint themselves when needed
 * 
 * @author Yura
 */
public class OrderingArrow extends JComponent {

    private static final long serialVersionUID = 1628286330969218453L;

    /**
     * Font for displaying sorting order
     */
    private static final Font FONT = new Font("Lucida Sans Regular", Font.PLAIN, 9);

    /**
     * Triangle that is displayed when ordering is ascending
     */
    private static final Shape ASC_TRIANGLE;

    /**
     * The width of the arrow that depicts the ordering
     */
    private static final float ARROW_WIDTH;

    /**
     * Triangle that is displayed when ordering is descending
     */
    private static final Shape DESC_TRIANGLE;

    private boolean drawNumber = true;

    private static final Dimension MIN_SIZE = new Dimension(12, 24);

    static {
        final float r = (float) MIN_SIZE.getWidth() / 2.0f - 1.0f;
        ARROW_WIDTH = r * (float) Math.sqrt(3.0);
        int degree = 90;
        double x = r * Math.cos(degree * Math.PI / 180.0), y = r * Math.sin(degree * Math.PI / 180.0);
        final double shiftY = MIN_SIZE.getHeight() / 2 - y;
        final GeneralPath originTriangle = new GeneralPath();
        originTriangle.moveTo(x, y);
        for (degree += 120; degree < 360; degree += 120) {
            x = r * Math.cos(degree * Math.PI / 180.0);
            y = r * Math.sin(degree * Math.PI / 180.0);
            originTriangle.lineTo(x, y);
        }
        originTriangle.closePath();

        DESC_TRIANGLE = new AffineTransform(1, 0, 0, 1, 0, shiftY + MIN_SIZE.getHeight() / 2.0f).createTransformedShape(originTriangle);
        ASC_TRIANGLE = new AffineTransform(1, 0, 0, -1, 0, -shiftY + MIN_SIZE.getHeight() / 2.0f).createTransformedShape(originTriangle);
    }

    /**
     * Triangle color when ordering is active (i.e. ascending or descending), and highlighted or not
     */
    private final Color activeColor = new Color(163, 186, 255); // light-blue

    private final Color activeHighlightColor = new Color(179, 198, 255);

    /**
     * Triangle color when ordering is inactive, and highlighted or not
     */
    private final Color inactiveColor = new Color(200, 200, 200);

    private final Color inactiveHighlightColor = new Color(207, 207, 207);

    /**
     * Current triangle active and inactive color
     */
    private Color currentActiveColor = activeColor;

    private Color currentInactiveColor = inactiveColor;

    /**
     * Is needed for drawing the component
     */
    private int order = 0;

    private SortOrder sortOrder = SortOrder.UNSORTED;

    /**
     * Creates instance of this component associated with specified {@link PropertyOrderingModel}. Adds mouse click event listener, which calls
     * {@link PropertyOrderingModel#clicked(boolean)} method when clicked
     * 
     * @param propertyOrdering
     */
    public OrderingArrow() {
        setMinimumSize(MIN_SIZE);
    }

    @Override
    public void paintComponent(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g;
        final Paint oldPaint = g2.getPaint();
        final Font oldFont = g2.getFont();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float shiftX = ARROW_WIDTH / 2.0f;

        if (!getSortOrder().equals(SortOrder.UNSORTED) && isDrawNumber()) {
            g2.setPaint(Color.black);
            g2.setFont(FONT);

            final String label = String.valueOf(getOrder());
            final FontRenderContext frc = g2.getFontRenderContext();
            final Rectangle2D bounds = FONT.getStringBounds(label, frc);
            final LineMetrics fontMetrics = FONT.getLineMetrics(label, frc);
            final float baseLineX = bounds.getWidth() > ARROW_WIDTH ? 0 : (float) Math.ceil(ARROW_WIDTH / 2.0f - bounds.getWidth() / 2.0f);
            final float baseLineY = (float) Math.ceil(MIN_SIZE.getHeight() / 2.0f - (float) bounds.getHeight() / 2 + fontMetrics.getAscent());
            g2.drawString(label, baseLineX, baseLineY);

            shiftX = bounds.getWidth() > ARROW_WIDTH ? (float) bounds.getWidth() / 2.0f : shiftX;
        }

        final boolean isDescending = SortOrder.DESCENDING.equals(getSortOrder());
        final boolean isActive = !SortOrder.UNSORTED.equals(getSortOrder());

        g2.translate(shiftX, 0);
        g2.setPaint((isActive && isDescending) ? currentActiveColor : currentInactiveColor);
        g2.fill(DESC_TRIANGLE);
        g2.setPaint((isActive && !isDescending) ? currentActiveColor : currentInactiveColor);
        g2.fill(ASC_TRIANGLE);

        g2.setFont(oldFont);
        g2.setPaint(oldPaint);
    }

    public boolean isDrawNumber() {
        return drawNumber;
    }

    public void setDrawNumber(final boolean drawNumber) {
        this.drawNumber = drawNumber;
        repaint();
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(final int order) {
        this.order = order;
        repaint();
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(final SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        repaint();
    }

    public void setMouseOver(final boolean isOver) {
        currentActiveColor = isOver ? activeHighlightColor : activeColor;
        currentInactiveColor = isOver ? inactiveHighlightColor : inactiveColor;
        repaint();
    }

    public double getActualWidth(final Graphics g) {
        if (g == null) {
            return MIN_SIZE.getWidth();
        }
        final Graphics2D g2 = (Graphics2D) g;
        final String label = String.valueOf(getOrder());
        final FontRenderContext frc = g2.getFontRenderContext();
        final Rectangle2D bounds = FONT.getStringBounds(label, frc);
        return bounds.getWidth() > ARROW_WIDTH ? (float) bounds.getWidth() : ARROW_WIDTH;
    }

    public double getActualHeight(final Graphics g) {
        return getMinimumSize().getHeight();
    }

}
