package ua.com.fielden.platform.swing.components.smart.datepicker;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Locale;

import javax.swing.JTextField;

import ua.com.fielden.platform.swing.components.smart.development.State;

public class DatePickerLogic extends AbstractMonthViewIntelliHints implements FocusListener {

    /**
     * This is the component responsible for rendering and handling events for smart button
     */
    private final DatePickerLayer layeredTextComponent;

    public DatePickerLogic(final DatePickerLayer datePickerLayer, final Locale locale, final Long defaultTimePortionMillis) {
	super(datePickerLayer.getView(), locale, defaultTimePortionMillis);
	this.layeredTextComponent = datePickerLayer;

	// When popup is displayed and user clicks on the text component then popup automatically hides, which leads to stale Accept state of the SmartButton.
	// In order to handle this situation textComponent needs a MouseListener to update SmartButton state.
	//
	// Previously this implementation was not taking into account the fact that textComponent receives mouse click also when the smart button is clicked.
	// This has been corrected by testing isMouseOver.
	getTextComponent().addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(final MouseEvent e) {
		super.mouseClicked(e);
		if (!isHintsPopupVisible() && !layeredTextComponent.getUi().isMouseOver()) {
		    State.NONE.next(layeredTextComponent.getUi());
		}
	    }
	});

	layeredTextComponent.getView().addFocusListener(this);
    }

    public void focusGained(final FocusEvent e) {
	State.NONE.next(layeredTextComponent.getUi());
    }

    public void focusLost(final FocusEvent e) {
	if ("*".equals(getTextComponent().getText())) {
	    getTextComponent().setText("");
	}
	layeredTextComponent.getUi().getState().next(layeredTextComponent.getUi());
    }

    //////////////////////////////////////// Similar to AutocompleterLogic logic ///////////////////////////////////////////////
    @Override
    public JTextField getTextComponent() {
	return (JTextField) super.getTextComponent();
    }

    /**
     * Overridden to clear selection.
     */
    @Override
    public void showHints() {
	popup();
	// clears selection date to make "month hints" usable for clicks and other manipulations.
	getMonthView().setSelectionDate(null);

	final Date selected = (Date) layeredTextComponent.getView().getValue();
	logger.debug("Selected date to be displayed in month view == " + selected);
	if (selected != null) {
	    // displays the day that was selected before
	    getMonthView().setFirstDisplayedDay(selected);
	    getMonthView().setFlaggedDates(selected);
	} else {
	    getMonthView().setFirstDisplayedDay(getMonthView().getToday());
	    getMonthView().clearFlaggedDates();
	}

	super.showHints();
	if (isHintsPopupVisible()) { // if popup is shown then this is the result of the search
	    State.SEARCH.next(layeredTextComponent.getUi());
	}
    }

    /**
     * <code>updateHints</code> incorporates the logic to determine the matching values for the provided context, and whether these values should be displayed. It also handles life
     * cycle of the progress indicator.
     */
    public boolean updateHints(final Object context) {
	return true;
    }

    @Override
    protected void hideHintsPopup() {
	super.hideHintsPopup();
	if (!isKeyTyped()) { // this indicates key ESC
	    State.NONE.next(layeredTextComponent.getUi());
	}
    }

    //////////////////////////////////////// End of [Similar to AutocompleterLogic logic] ///////////////////////////////////////////////
}
