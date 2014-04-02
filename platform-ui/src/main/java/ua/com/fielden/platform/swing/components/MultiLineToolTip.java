package ua.com.fielden.platform.swing.components;

//JMultiLineToolTip.java
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

/**
 * A {@link JToolTip} class descendant based on JTextArea implementation of tooltip component with capabilities of fixed maximum of char columns or pixel width.
 */

public class MultiLineToolTip extends JToolTip {
    private static final long serialVersionUID = 8401909800267693744L;

    private static final String uiClassID = "ToolTipUI";

    String tipText;
    JComponent component;
    protected int columns = 0;
    protected int fixedwidth = 0;

    public MultiLineToolTip() {
        updateUI();
    }

    @Override
    public void updateUI() {
        setUI(MultiLineToolTipUI.createUI(this));
    }

    public void setColumns(final int columns) {
        this.columns = columns;
        this.fixedwidth = 0;
    }

    public int getColumns() {
        return columns;
    }

    public void setFixedWidth(final int width) {
        this.fixedwidth = width;
        this.columns = 0;
    }

    public int getFixedWidth() {
        return fixedwidth;
    }
}

class MultiLineToolTipUI extends BasicToolTipUI {
    static MultiLineToolTipUI sharedInstance = new MultiLineToolTipUI();
    Font smallFont;
    static JToolTip tip;
    protected CellRendererPane rendererPane;

    private static JTextArea textArea;

    public static ComponentUI createUI(final JComponent c) {
        return sharedInstance;
    }

    public MultiLineToolTipUI() {
        super();
    }

    @Override
    public void installUI(final JComponent c) {
        super.installUI(c);
        tip = (JToolTip) c;
        rendererPane = new CellRendererPane();
        c.add(rendererPane);
    }

    @Override
    public void uninstallUI(final JComponent c) {
        super.uninstallUI(c);

        c.remove(rendererPane);
        rendererPane = null;
    }

    @Override
    public void paint(final Graphics g, final JComponent c) {
        final Dimension size = c.getSize();
        textArea.setBackground(c.getBackground());
        rendererPane.paintComponent(g, textArea, c, 1, 1, size.width - 1, size.height - 1, true);
    }

    @Override
    public Dimension getPreferredSize(final JComponent c) {
        final String tipText = ((JToolTip) c).getTipText();
        if (tipText == null)
            return new Dimension(0, 0);
        textArea = new JTextArea(tipText);
        rendererPane.removeAll();
        rendererPane.add(textArea);
        textArea.setWrapStyleWord(true);
        final int width = ((MultiLineToolTip) c).getFixedWidth();
        final int columns = ((MultiLineToolTip) c).getColumns();

        if (columns > 0) {
            textArea.setColumns(columns);
            textArea.setSize(0, 0);
            textArea.setLineWrap(true);
            textArea.setSize(textArea.getPreferredSize());
        } else if (width > 0) {
            textArea.setLineWrap(true);
            final Dimension d = textArea.getPreferredSize();
            d.width = width;
            d.height++;
            textArea.setSize(d);
        } else
            textArea.setLineWrap(false);

        final Dimension dim = textArea.getPreferredSize();

        dim.height += 1;
        dim.width += 1;
        return dim;
    }

    @Override
    public Dimension getMinimumSize(final JComponent c) {
        return getPreferredSize(c);
    }

    @Override
    public Dimension getMaximumSize(final JComponent c) {
        return getPreferredSize(c);
    }
}
