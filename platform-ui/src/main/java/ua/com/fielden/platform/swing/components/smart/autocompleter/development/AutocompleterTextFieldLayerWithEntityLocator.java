package ua.com.fielden.platform.swing.components.smart.autocompleter.development;

import javax.swing.JTextField;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.smart.autocompleter.renderer.development.TwoPropertyListCellRenderer;
import ua.com.fielden.platform.swing.review.report.centre.configuration.EntityLocatorDialog;

public class AutocompleterTextFieldLayerWithEntityLocator<T extends AbstractEntity> extends AutocompleterTextFieldLayer<T> {

    private static final long serialVersionUID = -5362041101618177143L;

    /**
     * Instantiates case sensitive autocompleter with wild card support. Please note that case sensitivity should be take into account by the valueMatcher.
     *
     * @param entityFactory
     *            TODO
     *
     */
    public AutocompleterTextFieldLayerWithEntityLocator(//
	    final EntityLocatorDialog<T, ?> entityLocatorDialog,//
	    final Class<T> lookUpClass,
	    final JTextField textComponent,//
	    final IValueMatcher<T> valueMatcher,//
	    final String expression,//
	    final TwoPropertyListCellRenderer<T> cellRenderer, //
	    final String caption,//
	    final String valueSeparator) {//
	this(entityLocatorDialog, lookUpClass, textComponent, valueMatcher, expression, cellRenderer, caption, valueSeparator, Settings.WILD_CARD_SUPPORT, Settings.CASE_SENSISTIVE);
    }

    /**
     * The most comprehensive constructor, which accepts the widest range of autocompleter parameters.
     *
     * @param textComponent
     *            -- used as a holder for selected values
     * @param valueMatcher
     *            -- provides the logic to identify what values match the criteria.
     * @param expression
     *            -- expression that is evaluated against a selected value; the result is displayed in the textComponent; can be null.
     * @param caption
     *            -- a short informative message, which is displayed on top of the textComponent if it is empty and not focused.
     * @param valueSeparator
     *            -- should a be non-null value is multi-selection to be supported.
     * @param settings
     *            -- autocompleter settings (case sensitive etc.).
     * @param lookupClass
     *            -- lookup instances type information
     */
    public AutocompleterTextFieldLayerWithEntityLocator(//
	    final EntityLocatorDialog<T, ?> entityLocatorDialog,//
	    final Class<T> lookUpClass,
	    final JTextField textComponent,//
	    final IValueMatcher<T> valueMatcher,//
	    final String expression,//
	    final TwoPropertyListCellRenderer<T> cellRenderer, //
	    final String caption,//
	    final String valueSeparator,//
	    final Settings... settings) { //
	super(textComponent, valueMatcher, lookUpClass, expression, cellRenderer, caption, valueSeparator, settings);
	new AutocompleterUiWithEntityLocator<T>(entityLocatorDialog, this, caption);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void highlightFirstHintValue(final boolean highlight) {
	((TwoPropertyListCellRenderer<T>) getAutocompleter().getHintsCellRenderer()).setHighlightFirstValue(highlight);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void highlightSecondHintValue(final boolean highlight) {
	((TwoPropertyListCellRenderer<T>) getAutocompleter().getHintsCellRenderer()).setHighlightSecondValue(highlight);
    }
}
