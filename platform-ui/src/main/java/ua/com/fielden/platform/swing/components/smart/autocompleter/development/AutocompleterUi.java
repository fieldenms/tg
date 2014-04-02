package ua.com.fielden.platform.swing.components.smart.autocompleter.development;

import javax.swing.JTextField;

import ua.com.fielden.platform.swing.components.smart.development.SmartComponentUi;

/**
 * Implement custom painting using LayerUI to display smart button: progress indicator, search, accept and none icons. It should also implement mouse event handler to process
 * onClick events in the are of the smart button.
 * 
 * @author 01es
 * 
 */
public class AutocompleterUi extends SmartComponentUi<JTextField, AutocompleterTextFieldLayer<?>> {
    public AutocompleterUi(final AutocompleterTextFieldLayer<?> layer, final String caption) {
        super(layer, caption, false);
    }

    /////////////////////////// DELEGATES //////////////////////
    @Override
    public boolean isHintsPopupVisible() {
        return getLayer().getAutocompleter().isHintsPopupVisible();
    }

    public boolean isMultiValued() {
        return getLayer().getAutocompleter().isMultiValued();
    }

    public String getValueSeparator() {
        return getLayer().getAutocompleter().getValueSeparator();
    }

    @Override
    public void showHintsPopup() {
        getLayer().getAutocompleter().showHintsPopup();
    }

    @Override
    public void performAcceptAction() {
        getLayer().getAutocompleter().performAcceptAction(getLayer().getAutocompleter());
    }
    /////////////////////// END OF DELEGATES //////////////////
}