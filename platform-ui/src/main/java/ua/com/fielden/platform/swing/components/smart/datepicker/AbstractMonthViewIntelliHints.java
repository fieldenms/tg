package ua.com.fielden.platform.swing.components.smart.datepicker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.plaf.UIManagerExt;

import ua.com.fielden.platform.swing.components.bind.development.BoundedJXDatePicker;
import ua.com.fielden.platform.swing.components.smart.development.AbstractIntelliHints;

/**
 * <code>AbstractMonthViewIntelliHints</code> extends AbstractIntelliHints and further implement most of the methods in interface {@link com.jidesoft.hints.IntelliHints}. In this
 * class, it assumes the hints can be represented as a JXMonthView, so it used JXMonthView in the hints popup.
 *
 * @author Jhou
 *
 */
public abstract class AbstractMonthViewIntelliHints extends AbstractIntelliHints {
    protected final Logger logger = Logger.getLogger(this.getClass());

    // private JList _list;
    private JXMonthView _monthView;
    /**
     * Popup that displays the month view with controls for traversing/selecting dates.
     */
    private JPanel _linkPanel;
    private MessageFormat _linkFormat;
    private Date linkDate;

    protected KeyStroke[] _keyStrokes;

    private final Long defaultTimePortionMillis;

    /**
     * Creates a Completion for JTextComponent
     *
     * @param textComponent
     */
    public AbstractMonthViewIntelliHints(final JTextComponent textComponent, final Locale locale, final Long defaultTimePortionMillis) {
	super(textComponent, null);

	this.defaultTimePortionMillis = defaultTimePortionMillis;

	setAutoPopup(false);

	init();
    }

    /**
     * Creates component to contain month view to select "hint" date from that component. Note that ListCellRenderer parameter absolutely ignored.
     */
    public JComponent createHintsComponent(final ListCellRenderer cellRenderer) {
	final JPanel p = new JPanel(new BorderLayout());
	p.add(_monthView = createMonthView(getTextComponent().getLocale()), BorderLayout.CENTER);
	getMonthView().setFocusable(false);
	p.setFocusable(false);
	init();
	p.add(getLinkPanel(), BorderLayout.SOUTH);
	// TODO : Hover.install(_monthView);
	return p;
    }

    /**
     * Creates the month view component to display selectable dates.
     *
     * @return the month view.
     */
    protected JXMonthView createMonthView(final Locale locale) {
	final JXMonthView mv = new JXMonthView(locale);
	//        _monthView.setSelectionModel(new SingleDaySelectionModel());s
	mv.setTraversable(true);
	mv.setFlaggedDayForeground(Color.GRAY);
	//        _monthView.addPropertyChangeListener(getMonthViewListener());
	return mv;
    }

    /**
     * Gets the month view.
     *
     * @return the month view.
     */
    protected JXMonthView getMonthView() {
	return _monthView;
    }

    public Object getSelectedHint() {
	return getMonthView().getSelectionDate();
    }

    @Override
    public JComponent getDelegateComponent() {
	return getMonthView();
    }

    @Override
    public void acceptHint(final Object selected) {
	if (selected == null) {
	    return;
	}
	final JFormattedTextField jftf = (JFormattedTextField) getTextComponent();
	jftf.setValue(BoundedJXDatePicker.modifyDateByTheTimePortion((Date) selected, (Date) jftf.getValue(), defaultTimePortionMillis));
    }

    /**
     * Gets the delegate keystrokes. Since we know the hints popup is a JList, we return eight keystrokes so that they can be delegate to the JList. Those keystrokes are DOWN, UP,
     * PAGE_DOWN, PAGE_UP, HOME and END.
     *
     * @return the keystokes that will be delegated to the JList when hints popup is visible.
     */
    @Override
    public KeyStroke[] getDelegateKeyStrokes() {
	if (_keyStrokes == null) {
	    _keyStrokes = new KeyStroke[6];
	    _keyStrokes[0] = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
	    _keyStrokes[1] = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
	    _keyStrokes[2] = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0);
	    _keyStrokes[3] = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0);
	    _keyStrokes[4] = KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0);
	    _keyStrokes[5] = KeyStroke.getKeyStroke(KeyEvent.VK_END, 0);
	}
	return _keyStrokes;
    }

    //////////////////////////////////////////////From JXDatePicker ////////////////////////////////////////
    private void init() {
	//        listenerMap = new EventListenerMap();
	updateLinkFormat();
	linkDate = getMonthView().getToday();
	_linkPanel = new TodayPanel();
	_linkPanel.setFocusable(false);
    }

    /**
     * @param _linkFormat
     *            the _linkFormat to set
     */
    protected void setLinkFormat(final MessageFormat _linkFormat) {
	this._linkFormat = _linkFormat;
    }

    /**
     * @return the _linkFormat
     */
    protected MessageFormat getLinkFormat() {
	return _linkFormat;
    }

    /**
     * Returns the date shown in the LinkPanel.
     * <p>
     * PENDING JW: the property should be named linkDate - but that's held by the deprecated long returning method. Maybe revisit if we actually remove the other.
     *
     * @return the date shown in the LinkPanel.
     */
    public Date getLinkDay() {
	return linkDate;
    }

    /**
     * Set the date the link will use and the string defining a MessageFormat to format the link. If no valid date is in the editor when the popup is displayed the popup will focus
     * on the month the linkDate is in. Calling this method will replace the currently installed linkPanel and install a new one with the requested date and format.
     *
     *
     * @param linkDay
     *            the Date to set on the LinkPanel
     * @param linkFormatString
     *            String used to format the link
     * @see java.text.MessageFormat
     */
    public void setLinkDay(final Date linkDay, final String linkFormatString) {
	setLinkFormat(new MessageFormat(linkFormatString));
	setLinkDay(linkDay);
    }

    /**
     * Sets the date shown in the TodayPanel.
     *
     * PENDING JW ... quick api hack for testing. Don't recreate the panel if it had been used
     *
     * @param linkDay
     *            the date used in the TodayPanel
     */
    public void setLinkDay(final Date linkDay) {
	this.linkDate = linkDay;
	final Format[] formats = getLinkFormat().getFormatsByArgumentIndex();
	for (final Format format : formats) {
	    if (format instanceof DateFormat) {
		((DateFormat) format).setTimeZone(getTimeZone());
	    }
	}
	//        TODO : setLinkPanel(new TodayPanel());
    }

    /**
     * Gets the time zone. This is a convenience method which returns the time zone of the JXMonthView being used.
     *
     * @return The <code>TimeZone</code> used by the <code>JXMonthView</code>.
     */
    public TimeZone getTimeZone() {
	return getMonthView().getTimeZone();
    }

    /**
     * Pes: added setLocale method to refresh link text on locale changes
     */
    private final class TodayPanel extends JXPanel {
	private static final long serialVersionUID = 1140302434729433784L;

	private TodayAction todayAction;
	private JXHyperlink todayLink;

	TodayPanel() {
	    super(new FlowLayout());
	    setBackgroundPainter(new MattePainter(new GradientPaint(0, 0, new Color(238, 238, 238), 0, 1, Color.WHITE)));
	    todayAction = new TodayAction();
	    todayLink = new JXHyperlink(todayAction);
	    todayLink.addMouseListener(createDoubleClickListener());
	    final Color textColor = new Color(16, 66, 104);
	    todayLink.setUnclickedColor(textColor);
	    todayLink.setClickedColor(textColor);
	    add(todayLink);
	}

	/**
	 * @return
	 */
	private MouseListener createDoubleClickListener() {
	    final MouseAdapter adapter = new MouseAdapter() {

		@Override
		public void mousePressed(final MouseEvent e) {
		    if (e.getClickCount() != 2)
			return;
		    todayAction.select = true;
		}

	    };
	    return adapter;
	}

	@Override
	protected void paintComponent(final Graphics g) {
	    super.paintComponent(g);
	    g.setColor(new Color(187, 187, 187));
	    g.drawLine(0, 0, getWidth(), 0);
	    g.setColor(new Color(221, 221, 221));
	    g.drawLine(0, 1, getWidth(), 1);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Overridden to update the link format and hyperlink text.
	 */
	@Override
	public void setLocale(final Locale l) {
	    super.setLocale(l);
	    updateLinkFormat();
	    todayLink.setText(getLinkFormat().format(new Object[] { getLinkDay() }));
	}

	private final class TodayAction extends AbstractAction {
	    private static final long serialVersionUID = 964906585238962899L;
	    boolean select;

	    TodayAction() {
		super(getLinkFormat().format(new Object[] { getLinkDay() }));
		final Calendar cal = getMonthView().getCalendar();
		cal.setTime(getLinkDay());
		putValue(NAME, getLinkFormat().format(new Object[] { cal.getTime() }));
	    }

	    public void actionPerformed(final ActionEvent ae) {
		logger.debug("Today action performed.");
		getMonthView().setFirstDisplayedDay(getMonthView().getToday());
		if (select) {
		    getMonthView().setSelectionDate(getMonthView().getToday());
		}
		select = false;
	    }
	}
    }

    /**
     * Update text on the link panel.
     *
     */
    private void updateLinkFormat() {
	// PENDING JW: move to ui
	final String linkFormat = UIManagerExt.getString("JXDatePicker.linkFormat", getTextComponent().getLocale());

	if (linkFormat != null) {
	    setLinkFormat(new MessageFormat(linkFormat));
	} else {
	    setLinkFormat(new MessageFormat("{0,date, dd MMMM yyyy}"));
	}
    }

    public JPanel getLinkPanel() {
	return _linkPanel;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected KeyStroke getShowHintsKeyStroke() {
	//	if (isMultilineTextComponent()) {
	return KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK);
	//	} else {
	//	    return KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
	//	}
    }

}
