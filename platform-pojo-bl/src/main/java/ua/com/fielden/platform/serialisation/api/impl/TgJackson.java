package ua.com.fielden.platform.serialisation.api.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;
import ua.com.fielden.platform.serialisation.jackson.EntityType;
import ua.com.fielden.platform.serialisation.jackson.EntityTypeInfoGetter;
import ua.com.fielden.platform.serialisation.jackson.EntityTypeProp;
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
import ua.com.fielden.platform.utils.EntityUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
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
public final class TgJackson extends ObjectMapper implements ISerialiserEngine {
    private static final long serialVersionUID = 8131371701442950310L;
    private final Logger logger = Logger.getLogger(getClass());

    private final TgJacksonModule module;
    private final EntityFactory factory;
    private final EntityTypeInfoGetter entityTypeInfoGetter;

    public TgJackson(final EntityFactory entityFactory, final ISerialisationClassProvider provider) {
        super();
        this.module = new TgJacksonModule();
        this.factory = entityFactory;
        entityTypeInfoGetter = new EntityTypeInfoGetter();

        // enable(SerializationFeature.INDENT_OUTPUT);
        // enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        registerEntityTypes(provider, this.module);

        this.module.addSerializer(Money.class, new MoneyJsonSerialiser());
        this.module.addDeserializer(Money.class, new MoneyJsonDeserialiser());

        this.module.addSerializer(Result.class, new ResultJsonSerialiser(this));
        this.module.addDeserializer(Result.class, new ResultJsonDeserialiser<Result>(this));
        this.module.addSerializer(Warning.class, new ResultJsonSerialiser(this));
        this.module.addDeserializer(Warning.class, new ResultJsonDeserialiser<Warning>(this));

        this.module.addDeserializer(ArrayList.class, new ArrayListJsonDeserialiser(this, entityTypeInfoGetter));
        this.module.addDeserializer((Class<List>) ClassesRetriever.findClass("java.util.Arrays$ArrayList"), new ArraysArrayListJsonDeserialiser(this, entityTypeInfoGetter));

        this.module.addSerializer(ICentreDomainTreeManagerAndEnhancer.class, new CentreManagerSerialiser(entityFactory));
        this.module.addSerializer(IPage.class, new PageSerialiser());

        registerModule(module);
    }

    /**
     * Register all serialisers / deserialisers for entity types present in TG app.
     */
    protected void registerEntityTypes(final ISerialisationClassProvider provider, final TgJacksonModule module) {
        new EntitySerialiser<EntityType>(EntityType.class, this.module, this, this.factory, entityTypeInfoGetter, true);
        new EntitySerialiser<EntityTypeProp>(EntityTypeProp.class, this.module, this, this.factory, entityTypeInfoGetter, true);
        for (final Class<?> type : provider.classes()) {
            if (AbstractEntity.class.isAssignableFrom(type)) {
                registerNewEntityType((Class<AbstractEntity<?>>) type);
            }
        }
    }

    /**
     * Registers the new type and returns the [number; EntityType].
     *
     * @param newType
     * @return
     */
    public EntityType registerNewEntityType(final Class<AbstractEntity<?>> newType) {
        return new EntitySerialiser<AbstractEntity<?>>(newType, module, this, factory, entityTypeInfoGetter).getEntityTypeInfo();
    }

    @Override
    public <T> T deserialise(final byte[] content, final Class<T> type) throws Exception {
        final ByteArrayInputStream bis = new ByteArrayInputStream(content);
        return deserialise(bis, type);
    }

    @Override
    public <T> T deserialise(final InputStream content, final Class<T> type) throws Exception {
        try {
            final String contentString = IOUtils.toString(content, "UTF-8");
            logger.debug("JSON before deserialisation = |" + contentString + "|.");

            final JavaType concreteType = extractConcreteType(getTypeFactory().constructType(type), () -> {
                try {
                    EntitySerialiser.getContext().reset();
                    return readTree(contentString).get("@id");
                } catch (final IOException e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }, entityTypeInfoGetter, getTypeFactory());

            EntitySerialiser.getContext().reset();
            final T val = readValue(contentString, concreteType);
            if (!DynamicEntityClassLoader.isEnhanced(concreteType.getRawClass())) {
                executeDefiners();
            }
            return val;
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Extracts concrete type for 'type' in case whether the 'type' is entity and abstract.
     *
     * @param type
     * @param idNodeSupplier
     *            -- the supplier function to retrieve idNode
     * @param entityTypeInfoGetter
     * @param typeFactory
     * @return
     */
    public static <T> JavaType extractConcreteType(final ResolvedType type, final Supplier<JsonNode> idNodeSupplier, final EntityTypeInfoGetter entityTypeInfoGetter, final TypeFactory typeFactory) {
        final JavaType concreteType;
        if (EntityUtils.isEntityType(type.getRawClass()) && Modifier.isAbstract(type.getRawClass().getModifiers())) {
            // when we are trying to deserialise an entity of unknown concrete type (e.g. passing AbstractEntity.class) -- there is a need to determine concrete type from @id property
            final JsonNode idNode = idNodeSupplier.get();
            if (idNode != null && !idNode.isNull()) {
                final String typeNumberStr = idNode.asText().split("#")[0];
                final Long typeNumber = Long.valueOf(typeNumberStr);
                final String concreteTypeName = entityTypeInfoGetter.get(typeNumber).getKey();
                concreteType = typeFactory.constructType(ClassesRetriever.findClass(concreteTypeName));
            } else {
                concreteType = (JavaType) type;
            }
        } else {
            concreteType = (JavaType) type;
        }
        return concreteType;
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

    public LinkedHashMap<Long, EntityType> getTypeTable() {
        return entityTypeInfoGetter.getTypeTable();
    }
}
