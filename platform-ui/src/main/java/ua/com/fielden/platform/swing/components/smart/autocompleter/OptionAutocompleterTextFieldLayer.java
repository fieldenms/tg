package ua.com.fielden.platform.swing.components.smart.autocompleter;

import javax.swing.JTextField;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.basic.autocompleter.DynamicEntityQueryCriteriaValueMatcher;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.swing.components.smart.autocompleter.renderer.TwoPropertyListCellRenderer;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;

public class OptionAutocompleterTextFieldLayer<T> extends AutocompleterTextFieldLayer<T> {

    /**
     * Instantiates case sensitive autocompleter with wild card support. Please note that case sensitivity should be take into account by the valueMatcher.
     * 
     * @param entityFactory
     *            TODO
     * 
     */
    public OptionAutocompleterTextFieldLayer(//
    final JTextField textComponent,//
    final EntityFactory entityFactory,//
    final IValueMatcher<T> valueMatcher,//
    final IEntityMasterManager entityMasterFactory, //
    final String expression,//
    final TwoPropertyListCellRenderer<T> cellRenderer, //
    final String caption,//
    final String valueSeparator,//
    final IValueMatcherFactory vmf,//
    final IDaoFactory daoFactory, final LocatorManager locatorManager) {//
	this(textComponent, entityFactory, valueMatcher, entityMasterFactory, expression, cellRenderer, caption, valueSeparator, vmf, daoFactory, locatorManager, Settings.WILD_CARD_SUPPORT, Settings.CASE_SENSISTIVE);
    }

    /**
     * The most comprehensive constructor, which accepts the widest range of autocompleter parameters.
     * 
     * @param textComponent
     *            -- used as a holder for selected values
     * @param entityFactory
     *            TODO
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
    public OptionAutocompleterTextFieldLayer(//
    final JTextField textComponent,//
    final EntityFactory entityFactory,//
    final IValueMatcher<T> valueMatcher,//
    final IEntityMasterManager entityMasterFactory, //
    final String expression,//
    final TwoPropertyListCellRenderer<T> cellRenderer, //
    final String caption,//
    final String valueSeparator,//
    final IValueMatcherFactory vmf,//
    final IDaoFactory daoFactory,//
    final LocatorManager locatorManager, final Settings... settings) { //
	super(textComponent, createExtendedValueMatcher(valueMatcher), (Class<T>) locatorManager.getResultantEntityClass(), expression, cellRenderer, caption, valueSeparator, settings);
	new OptionAutocompleterUi(entityFactory, this, caption, vmf, daoFactory, entityMasterFactory, locatorManager);
    }

    private static DynamicEntityQueryCriteriaValueMatcher createExtendedValueMatcher(final IValueMatcher valueMatcher) {
	return new DynamicEntityQueryCriteriaValueMatcher(valueMatcher);
    }

    @Override
    protected DynamicEntityQueryCriteriaValueMatcher getValueMatcher() {
	return (DynamicEntityQueryCriteriaValueMatcher) super.getValueMatcher();
    }

    public void highlightFirstHintValue(final boolean highlight) {
	((TwoPropertyListCellRenderer<T>) getAutocompleter().getHintsCellRenderer()).setHighlightFirstValue(highlight);
    }

    public void highlightSecondHintValue(final boolean highlight) {
	((TwoPropertyListCellRenderer<T>) getAutocompleter().getHintsCellRenderer()).setHighlightSecondValue(highlight);
    }
}
