package ua.com.fielden.platform.swing.components.smart.development;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterUi;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterUiWithEntityLocator;

/**
 * Defines SmartButton states and transitions rules.
 * 
 * @author 01es
 * 
 */
public enum State {
    NONE {
	@Override
	public synchronized void next(final SmartComponentUi ui) {
	    ui.setState(SEARCH);
	    ui.paintState();
	}

	@Override
	public void action(final SmartComponentUi ui) {
	}

	@Override
	public void ctrlAction(final AutocompleterUiWithEntityLocator ui) {
	}
    },
    PROGRESS {
	@Override
	public synchronized void next(final SmartComponentUi ui) {
	    if (ui.isHintsPopupVisible()) {
		ui.setState(ACCEPT);
	    } else if (ui.getComponent().hasFocus()) { // result set is empty and therefore no popup
		ui.setState(SEARCH);
	    } else { // this should mean that Autocompleter has lost the focus
		ui.setState(NONE);
	    }
	    ui.paintState();
	}

	@Override
	public void action(final SmartComponentUi ui) {
	}

	@Override
	public void ctrlAction(final AutocompleterUiWithEntityLocator ui) {

	}
    },
    SEARCH {
	@Override
	public synchronized void next(final SmartComponentUi ui) {
	    if (!ui.getComponent().hasFocus()) { // Autocompleter lost focus
		ui.setState(NONE);
	    } else if (ui.isHintsPopupVisible()) {
		ui.setState(ACCEPT);
	    } else {
		ui.setState(PROGRESS);
	    }
	    ui.paintState();
	}

	@Override
	public void action(final SmartComponentUi generalUi) {
	    if (generalUi instanceof AutocompleterUi) {
		final AutocompleterUi ui = (AutocompleterUi) generalUi;
		// if Autocompleter is empty or ends with a separator then put a wild card there
		if (StringUtils.isEmpty(ui.getComponent().getText()) || // Autocompleter is empty or
			(ui.isMultiValued() && ui.getComponent().getText().endsWith(ui.getValueSeparator()) && // autocompleter ends with separator and
				ui.getComponent().getCaretPosition() == ui.getComponent().getText().length())) { // cursor is at the end
		    ui.getComponent().setText(ui.getComponent().getText() + "*");
		}
	    }
	    generalUi.showHintsPopup();
	}

	@Override
	public void ctrlAction(final AutocompleterUiWithEntityLocator ui) {
	    PROGRESS.next(ui);
	    ui.getEntityLocatorDialog().showDialog();
	}
    },
    ACCEPT {
	@Override
	public synchronized void next(final SmartComponentUi ui) {
	    if (!ui.getComponent().hasFocus()) { // Autocompleter lost focus
		ui.setState(NONE);
	    } else if (!ui.isHintsPopupVisible()) {
		ui.setState(SEARCH);
	    } else {
		ui.setState(PROGRESS);
	    }
	    ui.paintState();
	}

	@Override
	public void action(final SmartComponentUi ui) {
	    ui.performAcceptAction();
	    NONE.next(ui);
	}

	@Override
	public void ctrlAction(final AutocompleterUiWithEntityLocator ui) {
	}
    };

    /**
     * Controls transition between states.
     * 
     * @param smartButton
     */
    public abstract void next(final SmartComponentUi ui);

    /**
     * Should implement state sensitive onClick action.
     * 
     * @param ui
     */
    public abstract void action(final SmartComponentUi ui);

    /**
     * Should implement state sensitive ctrl+onClick action.
     * 
     * @param ui
     */
    public abstract void ctrlAction(final AutocompleterUiWithEntityLocator ui);
}
