package ua.com.fielden.platform.serialisation.impl.serialisers;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import ua.com.fielden.platform.equery.QueryTokens;
import ua.com.fielden.platform.equery.tokens.main.ConditionsGroup;
import ua.com.fielden.platform.equery.tokens.main.GroupBy;
import ua.com.fielden.platform.equery.tokens.main.OrderBy;
import ua.com.fielden.platform.equery.tokens.main.Select;
import ua.com.fielden.platform.serialisation.impl.TgKryo;

import com.esotericsoftware.kryo.SerializationException;

/**
 * Serialises {@link QueryTokens} instances.
 * 
 * @author TG Team
 * 
 */
public class QueryTokensSerialiser extends TgSimpleSerializer<QueryTokens> {

    private final Field selectField;
    private final Field whereField;
    private final Field groupByField;
    private final Field orderByField;
    private final Field joinsField;
    private final Field resultTypeField;

    public QueryTokensSerialiser(final TgKryo kryo) {
	super(kryo);
	try {
	    selectField = QueryTokens.class.getDeclaredField("select");
	    selectField.setAccessible(true);
	    whereField = QueryTokens.class.getDeclaredField("where");
	    whereField.setAccessible(true);
	    groupByField = QueryTokens.class.getDeclaredField("groupBy");
	    groupByField.setAccessible(true);
	    orderByField = QueryTokens.class.getDeclaredField("orderBy");
	    orderByField.setAccessible(true);
	    joinsField = QueryTokens.class.getDeclaredField("joins");
	    joinsField.setAccessible(true);
	    resultTypeField = QueryTokens.class.getDeclaredField("resultType");
	    resultTypeField.setAccessible(true);
	} catch (final Exception e) {
	    throw new SerializationException("Could not obtain fields for QueryTokens type.");
	}
    }

    @Override
    public void write(final ByteBuffer buffer, final QueryTokens qm) {
	try {
	    writeValue(buffer, selectField.get(qm));
	    writeValue(buffer, groupByField.get(qm));
	    writeValue(buffer, orderByField.get(qm));
	    writeValue(buffer, joinsField.get(qm));
	    writeValue(buffer, resultTypeField.get(qm));
	    writeValue(buffer, whereField.get(qm));
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new SerializationException("Could not write QueryTokens: " + e.getMessage());
	}
    }

    @Override
    public QueryTokens read(final ByteBuffer buffer) {
	try {
	    final QueryTokens qt = new QueryTokens();
	    selectField.set(qt, readValue(buffer, Select.class));
	    groupByField.set(qt, readValue(buffer, GroupBy.class));
	    orderByField.set(qt, readValue(buffer, OrderBy.class));
	    joinsField.set(qt, readValue(buffer, ArrayList.class));
	    resultTypeField.set(qt, readValue(buffer, Class.class));
	    whereField.set(qt, readValue(buffer, ConditionsGroup.class));
	    return qt;
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new SerializationException("Could not read QueryTokens: " + e.getMessage());
	}
    }

}
