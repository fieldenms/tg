package ua.com.fielden.platform.serialisation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.util.ThreadSafeSimpleDateFormat;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * This an XStream converter for marshaling and unmarshaling {@link Date} instances.
 * 
 * @author 01es
 * 
 */
public class DateConverter implements Converter {

    private final ThreadSafeSimpleDateFormat defaultFormat;
    private final ThreadSafeSimpleDateFormat[] acceptableFormats;

    public DateConverter() {
        this(false);
    }

    /**
     * Construct a DateConverter with standard formats.
     * 
     * @param lenient
     *            the lenient setting of {@link SimpleDateFormat#setLenient(boolean)}
     */
    public DateConverter(final boolean lenient) {
        this("yyyy-MM-dd HH:mm:ss.S z", new String[] { "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.S", "yyyy-MM-dd HH:mm:ss.S a", "yyyy-MM-dd HH:mm:ssz", "yyyy-MM-dd HH:mm:ss z",
                "yyyy-MM-dd HH:mm:ssa" }, lenient);
    }

    public DateConverter(final String defaultFormat, final String[] acceptableFormats, final boolean lenient) {
        this.defaultFormat = new ThreadSafeSimpleDateFormat(defaultFormat, 4, 20, lenient);
        this.acceptableFormats = new ThreadSafeSimpleDateFormat[acceptableFormats.length];
        for (int i = 0; i < acceptableFormats.length; i++) {
            this.acceptableFormats[i] = new ThreadSafeSimpleDateFormat(acceptableFormats[i], 1, 20, lenient);
        }
    }

    @Override
    public void marshal(final Object obj, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        writer.setValue(defaultFormat.format((Date) obj));
    }

    @Override
    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        final String str = reader.getValue();
        try {
            return defaultFormat.parse(str);
        } catch (final ParseException e) {
            for (int i = 0; i < acceptableFormats.length; i++) {
                try {
                    return acceptableFormats[i].parse(str);
                } catch (final ParseException e2) {
                    // no worries, let's try the next format.
                }
            }
            // no dateFormats left to try
            throw new ConversionException("Cannot parse date " + str);
        }
    }

    @Override
    public boolean canConvert(final Class type) {
        return Date.class.equals(type);
    }
}
