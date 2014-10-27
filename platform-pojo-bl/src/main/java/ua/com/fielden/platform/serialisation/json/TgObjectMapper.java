package ua.com.fielden.platform.serialisation.json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.functional.centre.CritProp;
import ua.com.fielden.platform.entity.functional.centre.FetchProp;
import ua.com.fielden.platform.entity.functional.centre.QueryEntity;
import ua.com.fielden.platform.entity.functional.centre.QueryRunner;
import ua.com.fielden.platform.entity.functional.paginator.Page;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.serialisation.impl.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.json.deserialiser.JsonToAbstractEntityDeserialiser;
import ua.com.fielden.platform.serialisation.json.serialiser.AbstractEntityToJsonSerialiser;
import ua.com.fielden.platform.serialisation.json.serialiser.CentreManagerToJsonSerialiser;
import ua.com.fielden.platform.serialisation.json.serialiser.PageToJsonSerialiser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

public class TgObjectMapper extends ObjectMapper {

    private static final long serialVersionUID = 8131371701442950310L;

    private final TgModule module;

    @Inject
    public TgObjectMapper(final EntityFactory entityFactory, final ISerialisationClassProvider provider) {
        this(new SimpleDateFormat("dd/MM/yyyy hh:mma"), entityFactory, provider);
    }

    public TgObjectMapper(final DateFormat dateFormat, final EntityFactory entityFactory, final ISerialisationClassProvider provider) {
        super();
        this.module = new TgModule();

        //Configuring type specific parameters.
        setDateFormat(dateFormat);
        //enable(SerializationFeature.INDENT_OUTPUT);
        enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        //Configuring serialiser.
        addSerialiser(ICentreDomainTreeManagerAndEnhancer.class, new CentreManagerToJsonSerialiser(entityFactory));
        addSerialiser(IPage.class, new PageToJsonSerialiser());
        registerAbstractEntitySerialiser();

        //Configuring deserialiser.
        addDeserialiser(QueryRunner.class, new JsonToAbstractEntityDeserialiser<QueryRunner>(this, entityFactory));
        addDeserialiser(Page.class, new JsonToAbstractEntityDeserialiser<Page>(this, entityFactory));
        addDeserialiser(CritProp.class, new JsonToAbstractEntityDeserialiser<CritProp>(this, entityFactory));
        addDeserialiser(FetchProp.class, new JsonToAbstractEntityDeserialiser<FetchProp>(this, entityFactory));
        addDeserialiser(QueryEntity.class, new JsonToAbstractEntityDeserialiser<QueryEntity>(this, entityFactory));

        for (final Class<?> type : provider.classes()) {
            if (AbstractEntity.class.isAssignableFrom(type)) {
        	addDeserialiser(type, new JsonToAbstractEntityDeserialiser<>(this, entityFactory));
            }
        }

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
