package ua.com.fielden.platform.swing.components.blocking;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

/**
 * This is a pane, which is intended to be used instead of frame's GlassPane in order to provide blocking functionality. This implementation has been inspired by Romain Guy's
 * discussion about a blocking pane.
 * 
 * The usage is quite simply -- create a new instance where one of the constructor' parameters is a frame, which should be blocked. Invoke method start() whenever blocking is
 * necessary and stop() to stop blocking.
 * 
 * Please note that methods start(), stop() and setText(String) execute on EDT, and thus can be invoked directly.
 * 
 * Method setText(String) can be used to provide different massages to the user during blocking.
 * 
 * @author 01es
 * 
 */

public class BlockingIndefiniteProgressPane extends JComponent implements MouseListener, KeyListener {
    private static final long serialVersionUID = 5268059079543799729L;

    protected int alphaLevel = 200;

    private final InfiniteProgressPanel progressPanel;
    protected final JLabel label;

    protected final JRootPane rootPane;

    protected final RenderingHints hints;

    /**
     * This constructor is required mainly for backward compatibility reasons as well as a convenience.
     * 
     * @param originalMessage
     * @param frame
     */
    public BlockingIndefiniteProgressPane(final String originalMessage, final JFrame frame) {
	this(originalMessage, frame.getRootPane());
    }

    public BlockingIndefiniteProgressPane(final JRootPane rootPane) {
	this("", rootPane);
    }

    public BlockingIndefiniteProgressPane(final JFrame frame) {
	this("", frame);
    }

    public BlockingIndefiniteProgressPane(final String originalMessage, final JRootPane rootPane) {
	this(originalMessage, rootPane, 2.5d, 25);
    }

    public BlockingIndefiniteProgressPane(final String originalMessage, final JRootPane rootPane, final double scale, final int fontSize) {
	this.setBackground(new Color(55, 55, 55, alphaLevel));
	this.rootPane = rootPane;

	progressPanel = new InfiniteProgressPanel(scale) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public Dimension getPreferredSize() {
		return new Dimension(50, 50);
	    }
	};
	progressPanel.setOpaque(false);
	label = new JLabel(originalMessage);
	label.setFont(new Font("Default", 1, fontSize));
	label.setOpaque(false);
	setLayout(new MigLayout("wrap 1, fill", "[center]", "[b][t]"));
	add(progressPanel);
	add(label);

	hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    }

    @Override
    public void paintComponent(final Graphics g) {
	final Graphics2D g2 = (Graphics2D) g;
	g2.setRenderingHints(hints);

	g2.setColor(new Color(255, 255, 255, alphaLevel));
	g2.fillRect(0, 0, getWidth(), getHeight());

	super.paintComponent(g);
    }

    /**
     * Sets the text to be displayed and repaints the label. Executes on EDT.
     * 
     * @param text
     */
    public void setText(final String text) {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    public void run() {
		label.setText(text);
		repaint(label.getBounds()); // repaint just the area occupied by the label
	    }
	});
    }

    public String getText() {
	return label.getText();
    }

    private Component oldGlassPane;

    public void lock() {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    public void run() {
		oldGlassPane = rootPane.getGlassPane();
		rootPane.setGlassPane(BlockingIndefiniteProgressPane.this);
		progressPanel.start();
		rootPane.getParent().setEnabled(false);
		addKeyListener(BlockingIndefiniteProgressPane.this);
		addMouseListener(BlockingIndefiniteProgressPane.this);
		setVisible(true);
		setFocusable(true);
		requestFocusInWindow(true);
	    }
	});

    }

    public void unlock() {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    public void run() {
		progressPanel.stop();
		rootPane.getParent().setEnabled(true);
		removeMouseListener(BlockingIndefiniteProgressPane.this);
		removeKeyListener(BlockingIndefiniteProgressPane.this);
		setVisible(false);

		if (oldGlassPane != null) {
		    rootPane.setGlassPane(oldGlassPane);
		    oldGlassPane.setVisible(false);
		}
	    }
	});
    }

    public void mouseClicked(final MouseEvent e) {
	e.consume();
    }

    public void mousePressed(final MouseEvent e) {
	e.consume();
    }

    public void mouseReleased(final MouseEvent e) {
	e.consume();
    }

    public void mouseEntered(final MouseEvent e) {
	e.consume();
    }

    public void mouseExited(final MouseEvent e) {
	e.consume();
    }

    public void keyTyped(final KeyEvent e) {
	e.consume();
    }

    public void keyPressed(final KeyEvent e) {
	e.consume();
    }

    public void keyReleased(final KeyEvent e) {
	e.consume();
    }

    public static void main(final String[] args) throws Exception {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(600, 400));
		final BlockingIndefiniteProgressPane blockingPane = new BlockingIndefiniteProgressPane("blocking...", frame.getRootPane());
		frame.add(new JPanel(), BorderLayout.CENTER);
		final JPanel panel = new JPanel();
		final JButton button = new JButton("Start blocking");
		button.addActionListener(new ActionListener() {

		    public void actionPerformed(final ActionEvent e) {
			final SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>() {

			    @Override
			    protected Object doInBackground() throws Exception {
				blockingPane.setText("some message...");
				blockingPane.lock();
				Thread.sleep(1000);
				blockingPane.setText("message...");
				Thread.sleep(1000);
				return null;
			    }

			    @Override
			    protected void done() {
				blockingPane.unlock();
				super.done();
			    }
			};

			worker.execute();
		    }

		});
		panel.add(button);
		frame.add(panel, BorderLayout.SOUTH);
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	    }
	});
    }
}
