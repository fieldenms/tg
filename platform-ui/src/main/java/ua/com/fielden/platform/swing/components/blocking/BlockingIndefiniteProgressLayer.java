package ua.com.fielden.platform.swing.components.blocking;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.ext.LockableUI;
import org.jdesktop.swingx.JXHyperlink;

import ua.com.fielden.platform.dao.IComputationMonitor;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

/**
 * Layer for blocking JComponent instances. Convenient for blocking containers such as JPane or even individual buttons or text fields. Method {@link #setLocked(boolean)} should be
 * used to lock/unlock and method {@link #isLocked()} to test locking. Method {@link #setText(String)} allows updating of the message displayed on the blocking layer during
 * blocking.
 *
 * @author TG Team
 *
 */
public class BlockingIndefiniteProgressLayer extends JXLayer<JComponent> {
    private static final long serialVersionUID = 1L;

    private IBlockingLayerProvider provider;
    /**
     * Indicates whether "Incremental Locking" mode is turned on.
     */
    private boolean incrementalLocking = false;

    /**
     * Locking "balance" count while "Incremental Locking" mode is turned on.
     */
    private int count = 0;

    /**
     * Some processes can be cancelled by users. Thus, need to provide a button to trigger such an invent.
     */
    private final JXHyperlink btnCancel;

    /**
     * Creates a blocking layer for the passed component, which will display <code>message</code> when blocking.
     *
     * @param component
     * @param message
     */
    public BlockingIndefiniteProgressLayer(final JComponent component, final String message) {
	this(component, message, null);
    }

    /**
     * Creates a blocking layer for the passed component, which will display <code>message</code> when blocking.
     * If argument <code>monitor</code> is not <code>null</code> then a cancel action is created allowing users to cancel the computation is progress.
     *
     * @param component
     * @param message
     * @param monitor
     */
    public BlockingIndefiniteProgressLayer(final JComponent component, final String message, final IComputationMonitor monitor) {
	super(component, new BlockingUi(message));
	getGlassPane().setLayout(new MigLayout("fill", "[][c][]", "[t]"));

	btnCancel = monitor != null ? new JXHyperlink(createCancelAction(monitor)) : null;

    }

    private Action createCancelAction(final IComputationMonitor monitor) {
	final Command<Void> action = new Command<Void>("(cancel)") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected Void action(final ActionEvent event) throws Exception {
		monitor.stop();
		return null;
	    }

	    @Override
	    protected void postAction(final Void nothig) {
		super.postAction(nothig);
	    }
	};
	//action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
	//action.putValue(Action.LARGE_ICON_KEY, ResourceLoader.getIcon("images/password.png"));
	//action.putValue(Action.SMALL_ICON, ResourceLoader.getIcon("images/password.png"));
	action.putValue(Action.SHORT_DESCRIPTION, "Attempts to cancel currently running computations...");
	action.setEnabled(true);
	return action;
    }

    @Override
    public BlockingUi getUI() {
	return (BlockingUi) super.getUI();
    }

    /**
     * Convenient method for obtaining {@link IBlockingLayerProvider} instance for this blocking layer.
     *
     * @return
     */
    public final IBlockingLayerProvider provider() {
	if (provider == null) {
	    provider = new IBlockingLayerProvider() {
		@Override
		public BlockingIndefiniteProgressLayer getBlockingLayer() {
		    return BlockingIndefiniteProgressLayer.this;
		}
	    };
	}
	return provider;
    }

    /**
     * Set new message to be displayed during blocking. Executed on EDT.
     *
     * @param message
     */
    public void setText(final String message) {
	getUI().setText(message);
    }

    public String getText() {
	return getUI().getText();
    }

    /**
     * Delegates to {@link LockableUI#isLocked()}.
     *
     * @return
     */
    public boolean isLocked() {
	return getUI().isLocked();
    }

    /**
     * Turns on "Incremental Locking" mode.
     *
     */
    public void enableIncrementalLocking() {
	incrementalLocking = true;
	count = 0;
    }

    /**
     * Turns off "Incremental Locking" mode.
     *
     */
    private void disableIncrementalLocking() {
	incrementalLocking = false;
	count = 0;
    }

    /**
     * Returns true if "Incremental Locking" mode is turned on, false otherwise.
     *
     * @return
     */
    public boolean isIncrementalLocking() {
	return incrementalLocking;
    }

    /**
     * Indicates whether blocking panel has cancelling capability.
     *
     * @return
     */
    public boolean hasCancelCapability() {
	return btnCancel != null;
    }

    /**
     * Delegates to {@link LockableUI#setLocked(boolean)}.
     *
     * @return
     */
    public void setLocked(final boolean flag) {
	if (isIncrementalLocking()) {
	    count += flag ? 1 : -1;
	    //	    new Exception("layer.setLocked(" + flag + "). count == " + count).printStackTrace();
	    if (count < 0) {
		final String s = "Count of blocking " + count + " < 0.";
		JOptionPane.showMessageDialog(null, s);
		throw new RuntimeException(s);
	    } else if (count == 0) {
		// when the threshold value 0 is reached - "Incremental Locking" will be automatically turned off:
		unlock();
	    } else {
		getUI().setLocked(true);
		if (hasCancelCapability()) {
		    getGlassPane().add(btnCancel, "cell 1 1, gapy 80");
		}
	    }
	} else {
	    //	    System.out.println("Non-incremental locking: layer.setLocked(" + flag + ").");
	    getUI().setLocked(flag);
	    if (hasCancelCapability()) {
		if (flag) {
		    getGlassPane().add(btnCancel, "cell 1 1, gapy 80");
		} else {
		    getGlassPane().remove(btnCancel);
		}
	    }
	}
    }

    /**
     * Fully unlocks the panel even if it has an incremental nature.
     */
    public final void unlock() {
	count = 0;
	disableIncrementalLocking();
	getUI().setLocked(false);
	if (hasCancelCapability()) {
	    getGlassPane().remove(btnCancel);
	}
    }

    public static void main(final String[] args) {
	// the component to be locked
	final JPanel panel = new JPanel(new MigLayout("fill"));
	panel.add(new JTextField(), "growx");
	panel.add(new JButton("button"));
	// blocking layer
	final BlockingIndefiniteProgressLayer blocking = new BlockingIndefiniteProgressLayer(panel, "blocking...");
	// tool bar with buttons to lock
	final JPanel toolBar = new JPanel(new MigLayout("fill"));
	toolBar.add(new JButton(new AbstractAction("Lock/Unlock") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		blocking.setLocked(!blocking.isLocked());
	    }

	}));
	toolBar.add(new JButton(new AbstractAction("Change message") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		blocking.setText("Some random number: " + new Random().nextInt());
	    }

	}));

	// add the layer as any other component
	final JFrame frame = new JFrame("Test");
	frame.setLayout(new MigLayout("fill", "[]", "[][]"));
	frame.add(blocking, "grow, h :200:, wrap");
	frame.add(toolBar, "growx");
	frame.pack();
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setVisible(true);
    }

    /**
     * UI for locking JComponent instances. Based on LockableUI.
     */
    private static class BlockingUi extends LockableUI implements ActionListener {
	/**
	 * Message displayed directly under the progress indicator
	 */
	private String message;

	// /////// class members concerning progress painting ///////////////
	private static final int DEFAULT_NUMBER_OF_BARS = 12;
	private int numBars;
	private final double dScale = 1.5d;
	private Area[] bars;
	private Rectangle barsBounds = null;
	private AffineTransform centerAndScaleTransform = null;
	private final Timer timer = new Timer(1000 / 3, this);
	private Color[] colours = null;
	private int colorOffset = 0;
	private Font font = new JLabel("").getFont();

	public BlockingUi(final String caption) {
	    initProgress(dScale); // initialise progress indicator for Progress state
	    this.message = caption;
	}

	/**
	 * Overridden to switch timer drawing progress on/off.
	 */
	@Override
	public void setLocked(final boolean flag) {
	    super.setLocked(flag);
	    if (isLocked()) {
		timer.start();
	    } else {
		timer.stop();
	    }
	}

	/**
	 * Setter for changing text message while blocking.
	 *
	 * @param text
	 */
	public void setText(final String text) {
	    SwingUtilitiesEx.invokeLater(new Runnable() {
		@Override
		public void run() {
		    message = text;
		    setDirty(true);
		}
	    });
	}

	public String getText() {
	    return message;
	}

	/**
	 * If locked paints progress indicator and message underneath, fills component's bounds with white transparent colour.
	 */
	@Override
	protected void paintLayer(final Graphics2D g2, final JXLayer<JComponent> l) {
	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
	    super.paintLayer(g2, l); // this paints layer as is
	    if (isLocked()) {
		//
		final Rectangle bounds = getLayer().getView().getBounds();
		g2.setColor(new Color(255, 255, 255, 200));
		g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

		centerAndScaleTransform = new AffineTransform();
		centerAndScaleTransform.translate(bounds.x + bounds.width / 2d, bounds.y + bounds.height / 2d);
		centerAndScaleTransform.scale(dScale, dScale);

		final Area oBounds = new Area(barsBounds);
		oBounds.transform(centerAndScaleTransform);
		paintProgressTickers(g2);
		// clear the area designated to smart button

		g2.setColor(new Color(0f, 0f, 0f, 0.6f));

		g2.setFont(font);
		final FontMetrics fm = g2.getFontMetrics();
		final Rectangle2D textSize = fm.getStringBounds(message, g2);
		final double xPos = (bounds.width - textSize.getWidth()) / 2.;
		final double yPos = oBounds.getBounds().getY() + oBounds.getBounds().getHeight() + 10;
		g2.drawString(message, (float) xPos, (float) yPos);
	    }
	}

	// //////////////////////////////////////////////////////////
	// /////////////// progress painting logic //////////////////
	// //////////////////////////////////////////////////////////
	/**
	 * Called to animate the rotation of the bar's colours
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
	    // rotate colours
	    if (colorOffset == (numBars - 1)) {
		colorOffset = 0;
	    } else {
		colorOffset++;
	    }
	    // repaint layer
	    setDirty(true);
	}

	/**
	 * Builds the initial shape of the progress indicator.
	 *
	 * @param scale
	 */
	public void initProgress(final double scale) {
	    this.numBars = DEFAULT_NUMBER_OF_BARS;
	    colours = new Color[numBars * 2];
	    // build bars
	    bars = buildTicker(numBars);
	    // calculate bars bounding rectangle
	    barsBounds = new Rectangle();
	    for (final Area bar : bars) {
		barsBounds = barsBounds.union(bar.getBounds());
	    }
	    // create colours
	    for (int i = 0; i < bars.length; i++) {
		final int channel = 224 - 128 / (i + 1);
		colours[i] = new Color(channel, channel, channel);
		colours[numBars + i] = colours[i];
	    }
	}

	/**
	 * Paints progress indicator using passed Graphics instance.
	 *
	 * @param g
	 */
	protected void paintProgressTickers(final Graphics g) {
	    // move to center
	    final Graphics2D g2 = (Graphics2D) g.create();
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.transform(centerAndScaleTransform);
	    // draw ticker
	    for (int i = 0; i < bars.length; i++) {
		g2.setBackground(new Color(255, 255, 255, 255));
		g2.setColor(colours[i + colorOffset]);
		g2.fill(bars[i]);
	    }
	}

	/**
	 * Builds the circular shape and returns the result as an array of <code>Area</code>. Each <code>Area</code> is one of the bars composing the shape.
	 */
	private Area[] buildTicker(final int i_iBarCount) {
	    final Area[] ticker = new Area[i_iBarCount];
	    final Point2D.Double center = new Point2D.Double(0, 0);
	    final double fixedAngle = 2.0 * Math.PI / (i_iBarCount);

	    for (double i = 0.0; i < i_iBarCount; i++) {
		final Area primitive = buildPrimitive();

		final AffineTransform toCenter = AffineTransform.getTranslateInstance(center.getX(), center.getY());
		final AffineTransform toBorder = AffineTransform.getTranslateInstance(2.0, -0.4);
		final AffineTransform toCircle = AffineTransform.getRotateInstance(-i * fixedAngle, center.getX(), center.getY());

		final AffineTransform toWheel = new AffineTransform();
		toWheel.concatenate(toCenter);
		toWheel.concatenate(toBorder);

		primitive.transform(toWheel);
		primitive.transform(toCircle);

		ticker[(int) i] = primitive;
	    }

	    return ticker;
	}

	/**
	 * Builds a bar.
	 */
	private Area buildPrimitive() {
	    /*
	     * Rectangle2D.Double body = new Rectangle2D.Double(6, 0, 30, 12); Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 12, 12); Ellipse2D.Double tail = new
	     * Ellipse2D.Double(30, 0, 12, 12);
	     */

	    /*
	     * Rectangle2D.Double body = new Rectangle2D.Double(3, 0, 15, 6); Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 6, 6); Ellipse2D.Double tail = new
	     * Ellipse2D.Double(15, 0, 6, 6);
	     */

	    /*
	     * Rectangle2D.Double body = new Rectangle2D.Double(2, 0, 10, 4); Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 4, 4); Ellipse2D.Double tail = new
	     * Ellipse2D.Double(10, 0, 4, 4);
	     */

	    final Rectangle2D.Double body = new Rectangle2D.Double(0, 0, 6, 1);
	    // Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 2, 2);
	    // Ellipse2D.Double tail = new Ellipse2D.Double(5, 0, 2, 2);

	    final Area tick = new Area(body);
	    // tick.add(new Area(head));
	    // tick.add(new Area(tail));
	    return tick;
	}

	// ///////////////////// END OF PROGRESS PAINTING LOGIC ////////////////////
    }
}
