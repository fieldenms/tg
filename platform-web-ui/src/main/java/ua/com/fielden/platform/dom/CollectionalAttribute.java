package ua.com.fielden.platform.dom;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;


public class CollectionalAttribute<T> extends Attribute<List<T>> {

    protected final String separator;

    private final boolean quotes;

    public CollectionalAttribute(final String name, final List<T> value, final boolean quotes, final String nameValueSeparator, final String separator) {
	super(name, value, nameValueSeparator);
	this.separator = separator;
	this.quotes = quotes;
    }

    public CollectionalAttribute<T> addValue(final T attrValue) {
	value.add(attrValue);
	return this;
    }

    public CollectionalAttribute<T> removeValue(final T name) {
	value.remove(name);
	return this;
    }

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
