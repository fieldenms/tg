package ua.com.fielden.platform.serialisation.api.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.functional.centre.CritProp;
import ua.com.fielden.platform.entity.functional.centre.FetchProp;
import ua.com.fielden.platform.entity.functional.centre.QueryEntity;
import ua.com.fielden.platform.entity.functional.centre.QueryRunner;
import ua.com.fielden.platform.entity.functional.paginator.Page;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;
import ua.com.fielden.platform.serialisation.jackson.JacksonContext;
import ua.com.fielden.platform.serialisation.jackson.References;
import ua.com.fielden.platform.serialisation.jackson.TgJacksonModule;
import ua.com.fielden.platform.serialisation.jackson.deserialisers.ArrayListJsonDeserialiser;
import ua.com.fielden.platform.serialisation.jackson.deserialisers.ArraysArrayListJsonDeserialiser;
import ua.com.fielden.platform.serialisation.jackson.deserialisers.MoneyJsonDeserialiser;
import ua.com.fielden.platform.serialisation.jackson.deserialisers.ResultJsonDeserialiser;
import ua.com.fielden.platform.serialisation.jackson.serialisers.CentreManagerSerialiser;
import ua.com.fielden.platform.serialisation.jackson.serialisers.MoneyJsonSerialiser;
import ua.com.fielden.platform.serialisation.jackson.serialisers.PageSerialiser;
import ua.com.fielden.platform.serialisation.jackson.serialisers.ResultJsonSerialiser;
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

        // enable(SerializationFeature.INDENT_OUTPUT);
        // enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        this.module.addSerializer(Money.class, new MoneyJsonSerialiser());
        this.module.addDeserializer(Money.class, new MoneyJsonDeserialiser());

        this.module.addSerializer(Result.class, new ResultJsonSerialiser());
        this.module.addDeserializer(Result.class, new ResultJsonDeserialiser<Result>(this));
        this.module.addSerializer(Warning.class, new ResultJsonSerialiser());
        this.module.addDeserializer(Warning.class, new ResultJsonDeserialiser<Warning>(this));

        this.module.addDeserializer(ArrayList.class, new ArrayListJsonDeserialiser(this));
        this.module.addDeserializer((Class<List>) ClassesRetriever.findClass("java.util.Arrays$ArrayList"), new ArraysArrayListJsonDeserialiser(this));

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
            EntitySerialiser.getContext().reset();
            final T val = readValue(content, type);
            if (!DynamicEntityClassLoader.isEnhanced(type)) {
                executeDefiners();
            }
            return val;
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] serialise(final Object obj) {
        try {
            //            EntitySerialiser.getContext().reset();
            //            logger.error("Serialised pretty JSON = |" + new String(writerWithDefaultPrettyPrinter().writeValueAsBytes(obj), Charsets.UTF_8) + "|."); // TODO remove

            EntitySerialiser.getContext().reset();
            final byte[] bytes = writeValueAsBytes(obj); // default encoding is Charsets.UTF_8
            logger.debug("Serialised JSON = |" + new String(bytes, Charsets.UTF_8) + "|.");

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

    /**
     * All entity instances are cached during deserialisation.
     *
     * Once serialisation is completed it is necessary to execute respective definers for all cached instances.
     *
     * Definers cannot be executed inside {@link EntitySerialiser} due to the use of cache in conjunction with sub-requests issued by some of the definers leasing to an incorrect
     * deserialisation (specifically, object identifiers in cache get mixed up with the ones from newly obtained stream of data).
     *
     */
    private void executeDefiners() {
        final JacksonContext context = EntitySerialiser.getContext();
        final References references = (References) context.get(EntitySerialiser.ENTITY_JACKSON_REFERENCES);
        if (references != null) {
            // references is thread local variable, which gets reset if a nested deserialisation happens
            // therefore need to make a local cache of the present in references entities
            final Set<AbstractEntity<?>> refs = references.getNotEnhancedEntities();

            // explicit reset in order to make the reason for the above snippet more explicit
            references.reset();

            // iterate through all locally cached entity instances and execute respective definers
            for (final AbstractEntity<?> entity : refs) {
                entity.beginInitialising();
                for (final MetaProperty<?> prop : entity.getProperties().values()) {
                    if (prop != null) {
                        if (!prop.isCollectional()) {
                            prop.defineForOriginalValue();
                        }
                    }
                }
                entity.endInitialising();
            }
        }
    }
}
