package ua.com.fielden.platform.swing.components.smart.autocompleter.development;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.jxlayer.JXLayer;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.swing.components.smart.autocompleter.renderer.development.MultiplePropertiesListCellRenderer;

/**
 * This an autocompleter component, which is a JTextField wrapped into JXLayer. It utilises an instance of ValueMatcher to search for matching values.
 * 
 * @author 01es
 * 
 */
public class AutocompleterTextFieldLayer<T> extends JXLayer<JTextField> {
    /**
     * Enumeration that defines autocompletion parameters.
     * 
     * @author 01es
     * 
     */
    public static enum Settings {
        CASE_SENSISTIVE {
            @Override
            public void set(final AutocompleterLogic<?> autocompleterLogic) {
                autocompleterLogic.setCaseSensitive(true);
            }
        },
        WILD_CARD_SUPPORT {
            @Override
            public void set(final AutocompleterLogic<?> autocompleterLogic) {
                autocompleterLogic.setWhildcardSupport(true);
            }
        };

        public abstract void set(AutocompleterLogic<?> autocompleterLogic);
    }

    private static final long serialVersionUID = 1L;

    private final AutocompleterLogic<T> autocompleter;
    private final MultiplePropertiesListCellRenderer<T> cellRenderer;
    private final IValueMatcher<T> valueMatcher;
    private final String valueSeparator;

    /**
     * Instantiates case sensitive autocompleter with wild card support. Please note that case sensitivity should be take into account by the valueMatcher.
     * 
     */
    public AutocompleterTextFieldLayer(//
    final JTextField textComponent,//
            final IValueMatcher<T> valueMatcher,//
            final Class<T> lookupClass, //
            final String expression, //
            final MultiplePropertiesListCellRenderer<T> cellRenderer,//
            final String caption, //
            final String valueSeparator) {//
        this(textComponent, valueMatcher, lookupClass, expression, cellRenderer, caption, valueSeparator, Settings.WILD_CARD_SUPPORT, Settings.CASE_SENSISTIVE);
    }

    /**
     * The most comprehensive constructor, which accepts the widest range of autocompleter parameters.
     * 
     * @param textComponent
     *            -- used as a holder for selected values
     * @param valueMatcher
     *            -- provides the logic to identify what values match the criteria.
     * @param lookupClass
     *            -- lookup instances type information
     * @param expression
     *            -- expression that is evaluated against a selected value; the result is displayed in the textComponent; can be null.
     * @param caption
     *            -- a short informative message, which is displayed on top of the textComponent if it is empty and not focused.
     * @param valueSeparator
     *            -- should a be non-null value is multi-selection to be supported.
     * @param settings
     *            -- autocompleter settings (case sensitive etc.).
     */
    public AutocompleterTextFieldLayer(//
    final JTextField textComponent,//
            final IValueMatcher<T> valueMatcher,//
            final Class<T> lookupClass, //
            final String expression, //
            final MultiplePropertiesListCellRenderer<T> cellRenderer,//
            final String caption, //
            final String valueSeparator,//
            final Settings... settings) { //
        super(textComponent);
        this.valueSeparator = valueSeparator;
        this.valueMatcher = valueMatcher;
        this.cellRenderer = cellRenderer;
        autocompleter = new AutocompleterLogic<T>(this, valueSeparator, cellRenderer, lookupClass, expression) {
            @Override
            protected List<T> findMatches(final String value) {
                return valueMatcher.findMatches(value);
            }
        };
        // process settings
        for (final Settings setting : settings) {
            setting.set(autocompleter);
        }
        // instantiates UI and assigns it to this layer
        new AutocompleterUi(this, caption);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                getView().requestFocusInWindow();
            }
        });
    }

    AutocompleterUi getUi() {
        return (AutocompleterUi) super.getUI();
    }

    public AutocompleterLogic<T> getAutocompleter() {
        return autocompleter;
    }

    protected IValueMatcher<T> getValueMatcher() {
        return valueMatcher;
    }

    /**
     * Retrieves actual values based on the typed/selected text using the provided value matcher. The logic is as follows: if autocompleter supports wildcards then replace all
     * <code>*</code> with <code>%</code>; if there was no <code>*</code> then use value as is regardless of the whildcard support.
     * 
     * @return
     */
    public List<T> values() {
        return values(getView().getText());
    }

    /**
     * Returns a list of values for the given matching value.
     * 
     * @return
     */
    public List<T> values(final String forValue) {
        final String[] values = StringUtils.isEmpty(valueSeparator) ? new String[] { forValue } : forValue.split(valueSeparator);
        final List<T> result = new ArrayList<T>();
        final boolean hasWildcardSupport = autocompleter.hasWhildcardSupport();
        for (final String value : values) {
            final String searchFor = hasWildcardSupport && value.contains("*") ? value.replaceAll("\\*", "%") : value;
            final List<T> matches = getValueMatcher().findMatchesWithModel(searchFor);
            result.addAll(matches);
        }
        return result;
    }

    /**
     * Returns <code>true</code> if autocompleter supports entry of multiple values.
     * 
     * @return
     */
    public boolean isMulti() {
        return !StringUtils.isEmpty(getAutocompleter().getValueSeparator());
    }

    public void setEditable(final boolean flag) {
        getView().setEditable(flag);
    }

    public void setPropertyToHighlight(final String exprProperty, final boolean highlight) {
        cellRenderer.setPropertyToHighlight(exprProperty, highlight);
    }
}
