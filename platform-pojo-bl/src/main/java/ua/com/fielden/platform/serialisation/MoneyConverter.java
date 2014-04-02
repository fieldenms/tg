package ua.com.fielden.platform.serialisation;

import java.util.Currency;

import ua.com.fielden.platform.types.Money;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * This an XStream converter for marshaling and unmarshaling instances of {@link Money}.
 * 
 * @author 01es
 * 
 */
public class MoneyConverter implements Converter {

    public MoneyConverter() {
    }

    @Override
    public void marshal(final Object obj, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final Money amount = (Money) obj;
        writer.addAttribute("amount", amount.getAmount().toString());
        writer.addAttribute("currency", amount.getCurrency().toString());
        if (amount.getTaxPercent() != null) {
            writer.addAttribute("tax", amount.getTaxPercent().toString());
        }
    }

    @Override
    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        final String amount = reader.getAttribute("amount");
        final String currency = reader.getAttribute("currency");
        final String taxPercent = reader.getAttribute("tax");
        if (taxPercent == null) {
            return new Money(amount, Currency.getInstance(currency));
        } else {
            return new Money(amount, Integer.parseInt(taxPercent), Currency.getInstance(currency));
        }
    }

    @Override
    public boolean canConvert(final Class type) {
        return Money.class.equals(type);
    }
}
