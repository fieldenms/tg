package ua.com.fielden.platform.utils;

import static java.text.NumberFormat.getNumberInstance;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils.ShowingStrategy;

/**
 * Simple converter factory for creating converters e.g. from Object (e.g. Integer, BigDecimal) to String. Used in ComponentFactory
 * 
 * @author jhou
 * 
 */
public class ConverterFactory {

    public static abstract class Converter {

	public abstract String convertToString(final Object value);

	public abstract Object convertToObject(final String value);

    }

    private ConverterFactory() {
    }

    public static final Converter createNumberConverter() {
	return new NumberConverter();
    }

    public static final Converter createMoneyConverter() {
	return new MoneyConverter();
    }

    public static final Converter createAbstractEntityOrListConverter(final ShowingStrategy showingStrategy) {
	return new AbstractEntityOrListConverter(showingStrategy);
    }

    public static final Converter createDateConverter() {
	return new DateConverter();
    }

    public static final Converter createTrivialConverter() {
	return new TrivialConverter();
    }

    public static final Converter createStringListConverter() {
	return new StringListConverter();
    }

    private static class NumberConverter extends Converter {

	@Override
	public String convertToString(final Object value) {
	    if (value != null) {
		return getNumberInstance().format(value);
	    } else {
		return null;
	    }
	}

	@Override
	public Integer convertToObject(final String value) {
	    if (StringUtils.isEmpty(value)) {
		return 0;
	    }
	    try {
		return getNumberInstance().parse(value).intValue();
	    } catch (final ParseException e) {
		throw new RuntimeException(e);
	    }
	}

    }

    private static class MoneyConverter extends Converter {

	@Override
	public String convertToString(final Object value) {
	    final Money money = (Money) value;
	    if (money == null) {
		return null;
	    }
	    return money.toString();
	}

	@Override
	public Money convertToObject(final String value) {
	    if (StringUtils.isEmpty(value)) {
		return new Money(BigDecimal.valueOf(0));
	    }
	    final BigDecimal bigDecimal = new BigDecimal(value);
	    return new Money(bigDecimal);
	}

    }

    private static class AbstractEntityOrListConverter extends Converter {

	final ShowingStrategy showingStrategy;

	protected AbstractEntityOrListConverter() {
	    super();
	    this.showingStrategy = null;
	}

	public AbstractEntityOrListConverter(final ShowingStrategy showingStrategy) {
	    super();
	    this.showingStrategy = showingStrategy;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String convertToString(final Object value) {
	    if (value != null) {
		String htmlString = null;
		if (value instanceof List) {
		    if (((List<?>) value).isEmpty()) {
			htmlString = "";
		    } else {
			final StringBuffer s = new StringBuffer("<html>");
			for (final AbstractEntity<?> entity : (List<AbstractEntity>) value) {
			    s.append(entityString(entity));
			}
			s.append("</html>");
			htmlString = s.toString();
		    }
		} else {
		    final StringBuffer s = new StringBuffer("<html>" + entityString((AbstractEntity<?>) value) + "</html>");
		    htmlString = s.toString();
		}
		return htmlString;
	    } else {
		return null;
	    }
	}

	@SuppressWarnings("rawtypes")
	private final String entityString(final AbstractEntity entity) {
	    return (showingStrategy == ShowingStrategy.KEY_ONLY) ? entity.getKey() + "<br>" : ((showingStrategy == ShowingStrategy.KEY_AND_DESC) ? (entity.getKey() + "<br><i>"
		    + entity.getDesc() + "</i></br>") : ("<i>" + entity.getDesc() + "</i><br>"));
	}

	@Override
	public AbstractEntity<?> convertToObject(final String value) {
	    // TODO implement
	    return null;
	}

    }

    private static class TrivialConverter extends Converter {

	@Override
	public String convertToString(final Object value) {
	    if (value != null) {
		return value.toString();
	    } else {
		return null;
	    }
	}

	@Override
	public Date convertToObject(final String value) {
	    // TODO implement
	    return null;
	}

    }

    private static class DateConverter extends Converter {

	private static final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mma");

	@Override
	public String convertToString(final Object value) {
	    final Date date = value == null ? null : (value instanceof Date ? (Date) value : ((DateTime) value).toDate());
	    if (date != null) {
		return format.format(date);
	    } else {
		return null;
	    }
	}

	@Override
	public Date convertToObject(final String value) {
	    // TODO implement
	    return null;
	}

    }

    private static class StringListConverter extends Converter {

	@Override
	@SuppressWarnings("unchecked")
	public String convertToString(final Object value) {
	    if (value != null) {
		String htmlString = null;
		if (((List<String>) value).isEmpty()) {
		    htmlString = "";//"empty list";
		} else {
		    final StringBuffer s = new StringBuffer("<html>");
		    for (final String str : (List<String>) value) {
			s.append(str + "<br>");
		    }
		    s.append("</html>");
		    htmlString = s.toString();
		}
		return htmlString;
	    } else {
		return null;
	    }
	}

	@Override
	public AbstractEntity<?> convertToObject(final String value) {
	    // TODO implement
	    return null;
	}

    }

}
