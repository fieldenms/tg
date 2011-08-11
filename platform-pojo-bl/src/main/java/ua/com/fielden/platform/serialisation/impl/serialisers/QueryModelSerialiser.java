package ua.com.fielden.platform.serialisation.impl.serialisers;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.equery.QueryModel;
import ua.com.fielden.platform.equery.QueryTokens;
import ua.com.fielden.platform.serialisation.impl.TgKryo;

import com.esotericsoftware.kryo.SerializationException;

/**
 * Serialises {@link QueryModel} instances.
 * 
 * @author TG Team
 * 
 */
public class QueryModelSerialiser extends TgSimpleSerializer<QueryModel> {

    private final Field tokensField;
    private final Field parametersField;
    private final Field resultTypeField;
    private final Field lightweightField;


    public QueryModelSerialiser(final TgKryo kryo) {
	super(kryo);
	try {
	    tokensField = QueryModel.class.getDeclaredField("tokens");
	    tokensField.setAccessible(true);
	    resultTypeField = QueryModel.class.getDeclaredField("resultType");
	    resultTypeField.setAccessible(true);
	    parametersField = QueryModel.class.getDeclaredField("parameters");
	    parametersField.setAccessible(true);
	    lightweightField = QueryModel.class.getDeclaredField("lightweight");
	    lightweightField.setAccessible(true);
	} catch (final Exception e) {
	    throw new SerializationException("Could not obtain fields for QueryModel type.");
	}
    }

    @Override
    public void write(final ByteBuffer buffer, final QueryModel qm) {
	try {
	    writeValue(buffer, resultTypeField.get(qm));
	    writeValue(buffer, parametersField.get(qm));
	    writeBoolean(buffer, (Boolean) lightweightField.get(qm));
	    writeValue(buffer, tokensField.get(qm));
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new SerializationException("Could not write QueryModel: " + e.getMessage());
	}
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public QueryModel read(final ByteBuffer buffer) {
	try {
	    final Class rt = readValue(buffer, Class.class);
	    final Map parameters = readValue(buffer, HashMap.class);
	    final Boolean lightweight = readBoolean(buffer);
	    final QueryTokens qt = readValue(buffer, QueryTokens.class);

	    final QueryModel qm = new QueryModel(qt, rt);
	    if (parameters != null) {
		final Map params = (Map) parametersField.get(qm);
		params.putAll(parameters);
	    }
	    qm.setLightweight(lightweight);

	    return qm;
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new SerializationException("Could not read QueryModel: " + e.getMessage());
	}
    }

}
