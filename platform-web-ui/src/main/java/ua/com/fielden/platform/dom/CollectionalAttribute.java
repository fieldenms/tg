package ua.com.fielden.platform.dom;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * The {@link Attribute} which value is a collection.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class CollectionalAttribute<T> extends Attribute<List<T>> {

    /**
     * Separator between values of collection.
     */
    protected final String separator;

    /**
     * Indicates whether attribute value should be quoted or not.
     */
    private final boolean quotes;

    public CollectionalAttribute(final String name, final List<T> value, final boolean quotes, final String nameValueSeparator, final String separator) {
	super(name, value, nameValueSeparator);
	this.separator = separator;
	this.quotes = quotes;
    }

    /**
     * Adds value to the collection.
     *
     * @param attrValue
     * @return
     */
    public CollectionalAttribute<T> addValue(final T attrValue) {
	value.add(attrValue);
	return this;
    }

    /**
     * Removes the value from collection.
     *
     * @param name
     * @return
     */
    public CollectionalAttribute<T> removeValue(final T attrValue) {
	value.remove(attrValue);
	return this;
    }

    /**
     * Returns values those are in the collection.
     *
     * @param values
     * @return
     */
    @SuppressWarnings("unchecked")
    public CollectionalAttribute<T> values(final T... values) {
	value.clear();
	value.addAll(Arrays.asList(values));
	return this;
    }

    @Override
    public String toString() {
	final String strValue = StringUtils.join(value, separator);
	final String quoteString = quotes ? "\"" : "";
        return StringUtils.isEmpty(strValue) ? "" : (name + nameValueSeparator + quoteString + strValue + quoteString);
    }
}
