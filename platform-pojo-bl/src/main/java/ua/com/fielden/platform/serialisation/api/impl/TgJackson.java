package ua.com.fielden.platform.serialisation.api.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.functional.centre.CritProp;
import ua.com.fielden.platform.entity.functional.centre.FetchProp;
import ua.com.fielden.platform.entity.functional.centre.QueryEntity;
import ua.com.fielden.platform.entity.functional.centre.QueryRunner;
import ua.com.fielden.platform.entity.functional.paginator.Page;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;
import ua.com.fielden.platform.serialisation.jackson.TgJacksonModule;
import ua.com.fielden.platform.serialisation.jackson.deserialisers.MoneyJsonDeserialiser;
import ua.com.fielden.platform.serialisation.jackson.serialisers.CentreManagerSerialiser;
import ua.com.fielden.platform.serialisation.jackson.serialisers.MoneyJsonSerialiser;
import ua.com.fielden.platform.serialisation.jackson.serialisers.PageSerialiser;
import ua.com.fielden.platform.types.Money;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;

/**
 * The descendant of {@link ObjectMapper} with TG specific logic to correctly assign serialisers and recognise descendants of {@link AbstractEntity}. This covers correct
 * determination of the underlying entity type for dynamic CGLIB proxies.
 * <p>
 * All classes have to be registered at the server ({@link TgJackson}) and client ('tg-serialiser' web component) sides in the same order. To be more specific -- the 'type table'
 * at the server and client side should be identical (most likely should be send to the client during client application startup).
 *
 * @author TG Team
 *
 */
final class TgJackson extends ObjectMapper implements ISerialiserEngine {
    private static final long serialVersionUID = 8131371701442950310L;
    private final Logger logger = Logger.getLogger(getClass());

    private final TgJacksonModule module;
    private final EntityFactory factory;

    public TgJackson(final EntityFactory entityFactory, final ISerialisationClassProvider provider) {
        super();
        this.module = new TgJacksonModule();
        this.factory = entityFactory;

        // Configuring type specific parameters.
        // setDateFormat(dateFormat);

        // enable(SerializationFeature.INDENT_OUTPUT);
        // enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        this.module.addSerializer(Money.class, new MoneyJsonSerialiser());
        this.module.addDeserializer(Money.class, new MoneyJsonDeserialiser());

        this.module.addSerializer(ICentreDomainTreeManagerAndEnhancer.class, new CentreManagerSerialiser(entityFactory));
        this.module.addSerializer(IPage.class, new PageSerialiser());

        registerEntityTypes(provider, this.module);

        registerModule(module);
    }

    /**
     * Register all serialisers / deserialisers for entity types present in TG app.
     */
    protected void registerEntityTypes(final ISerialisationClassProvider provider, final TgJacksonModule module) {
        // registerAbstractEntitySerialiser();

        new EntitySerialiser<QueryRunner>(QueryRunner.class, this.module, this, this.factory);
        new EntitySerialiser<Page>(Page.class, this.module, this, this.factory);
        new EntitySerialiser<CritProp>(CritProp.class, this.module, this, this.factory);
        new EntitySerialiser<FetchProp>(FetchProp.class, this.module, this, this.factory);
        new EntitySerialiser<QueryEntity>(QueryEntity.class, this.module, this, this.factory);

        for (final Class<?> type : provider.classes()) {
            if (AbstractEntity.class.isAssignableFrom(type)) {
                new EntitySerialiser<AbstractEntity<?>>((Class<AbstractEntity<?>>) type, this.module, this, this.factory);
            }
        }
    }

    //
    //    protected void registerAbstractEntitySerialiser() {
    //        addSerialiser(AbstractEntity.class, new EntitySerialiser());
    //    }

    @Override
    public <T> T deserialise(final byte[] content, final Class<T> type) throws Exception {
        final ByteArrayInputStream bis = new ByteArrayInputStream(content);
        return deserialise(bis, type);
    }

    @Override
    public <T> T deserialise(final InputStream content, final Class<T> type) throws Exception {
        try {
            return readValue(content, type);
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] serialise(final Object obj) {
        try {
            final byte[] bytes = writeValueAsBytes(obj); // default encoding is Charsets.UTF_8
            logger.error("Serialised pretty JSON = [" + writerWithDefaultPrettyPrinter().writeValueAsString(obj) + "]."); // TODO remove
            logger.debug("Serialised JSON = [" + new String(bytes, Charsets.UTF_8) + "].");
            return bytes;
        } catch (final JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public EntityFactory factory() {
        return factory;
    }
}
