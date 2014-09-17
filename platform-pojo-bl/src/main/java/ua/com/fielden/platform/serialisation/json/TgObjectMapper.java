package ua.com.fielden.platform.serialisation.json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.serialisation.json.deserialiser.JsonToCentreConfigDeserialiser;
import ua.com.fielden.platform.serialisation.json.deserialiser.JsonToCentreConfigDeserialiser.LightweightCentre;
import ua.com.fielden.platform.serialisation.json.deserialiser.JsonToCriteriaDeserialiser;
import ua.com.fielden.platform.serialisation.json.deserialiser.JsonToCriteriaDeserialiser.CritProp;
import ua.com.fielden.platform.serialisation.json.deserialiser.JsonToResultDeserialiser;
import ua.com.fielden.platform.serialisation.json.deserialiser.JsonToResultDeserialiser.ResultProperty;
import ua.com.fielden.platform.serialisation.json.serialiser.AbstractEntityToJsonSerialiser;
import ua.com.fielden.platform.serialisation.json.serialiser.CentreMangerToJsonSerialiser;
import ua.com.fielden.platform.serialisation.json.serialiser.PageToJsonSerialiser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TgObjectMapper extends ObjectMapper {

    private static final long serialVersionUID = 8131371701442950310L;

    private final TgModule module;

    public TgObjectMapper() {
        this(new SimpleDateFormat("dd/MM/yyyy hh:mma"));
    }

    public TgObjectMapper(final DateFormat dateFormat) {
        super();
        module = new TgModule();

        // Configuring type specific parameters.
        setDateFormat(dateFormat);
        //enable(SerializationFeature.INDENT_OUTPUT);
        enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        //Configuring serialiser.
        addSerialiser(ICentreDomainTreeManagerAndEnhancer.class, new CentreMangerToJsonSerialiser());
        addSerialiser(IPage.class, new PageToJsonSerialiser());
        registerAbstractEntitySerialiser();

        //Configuring deserialiser.
        addDeserialiser(LightweightCentre.class, new JsonToCentreConfigDeserialiser(this));
        addDeserialiser(CritProp.class, new JsonToCriteriaDeserialiser(this));
        addDeserialiser(ResultProperty.class, new JsonToResultDeserialiser(this));

        //Registering module.
        registerModule(module);
    }

    protected void registerAbstractEntitySerialiser() {
        addSerialiser(AbstractEntity.class, new AbstractEntityToJsonSerialiser());
    }

    public <T> void addSerialiser(final Class<? extends T> clazz, final JsonSerializer<T> serializer) {
        module.addSerializer(clazz, serializer);
    }

    public <T> void addDeserialiser(final Class<T> clazz, final JsonDeserializer<? extends T> deserializer) {
        module.addDeserializer(clazz, deserializer);
    }

}
