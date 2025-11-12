package ua.com.fielden.platform.serialisation.api.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.continuation.NeedMoreDataException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.security.user.UserSecret;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.exceptions.SerialisationException;
import ua.com.fielden.platform.serialisation.jackson.*;
import ua.com.fielden.platform.serialisation.jackson.deserialisers.*;
import ua.com.fielden.platform.serialisation.jackson.exceptions.EntityDeserialisationException;
import ua.com.fielden.platform.serialisation.jackson.exceptions.EntitySerialisationException;
import ua.com.fielden.platform.serialisation.jackson.serialisers.*;
import ua.com.fielden.platform.types.*;
import ua.com.fielden.platform.utils.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.fasterxml.jackson.databind.type.SimpleType.constructUnsafe;
import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;

/// A subclass of [ObjectMapper] with TG-specific logic to correctly assign serialisers and recognise subtypes of [AbstractEntity].
/// This covers correct determination of the underlying entity type for dynamic CGLIB proxies.
///
/// All classes have to be registered at the server ([TgJackson]) and client (`tg-serialiser` web component) sides in the same order.
/// Specifically, the "type table" at the server and client side should be identical (most likely should be sent to the client during client application startup).
///
public final class TgJackson extends ObjectMapper implements ISerialiserEngine {
    private static final long serialVersionUID = 8131371701442950310L;
    private static final Logger logger = getLogger();

    public static final String ERR_RESTRICTED_TYPE_SERIALISATION = "Type [%s] is not permitted for serialisation.";
    public static final String ERR_RESTRICTED_TYPE_DESERIALISATION = "Type [%s] is not permitted for deserialisation.";
    
    private final TgJacksonModule module;
    private final EntityFactory factory;
    private final EntityTypeInfoGetter entityTypeInfoGetter;
    private final ISerialisationTypeEncoder serialisationTypeEncoder;
    public final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache;
    
    //private final LRUMap<?, ?> cachedFCAsToClear; EXPERIMENTAL

    public TgJackson(final EntityFactory entityFactory, final ISerialisationClassProvider provider, final ISerialisationTypeEncoder serialisationTypeEncoder, final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache) {
        this.module = new TgJacksonModule(this);
        this.factory = entityFactory;
        entityTypeInfoGetter = new EntityTypeInfoGetter();
        this.serialisationTypeEncoder = serialisationTypeEncoder.setTgJackson(this);
        this.idOnlyProxiedEntityTypeCache = idOnlyProxiedEntityTypeCache;

        // Gracefully serialise getters with self-references.
        // This allows Result.ex serialisation with different types of causes potentially coming from external libs.
        disable(SerializationFeature.FAIL_ON_SELF_REFERENCES);
        enable(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL);

        // enable(SerializationFeature.INDENT_OUTPUT);
        // enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

        registerEntityTypes(provider, this.module);

        // If a serializer is registered for a supertype, it will also be used for serialization of its subtypes,
        // unless a more specific serializer is registered.

        this.module.addSerializer(Money.class, new MoneyJsonSerialiser());
        this.module.addDeserializer(Money.class, new MoneyJsonDeserialiser());
        
        this.module.addSerializer(Colour.class, new ColourJsonSerialiser());
        this.module.addDeserializer(Colour.class, new ColourJsonDeserialiser());
        
        this.module.addSerializer(Hyperlink.class, new HyperlinkJsonSerialiser());
        this.module.addDeserializer(Hyperlink.class, new HyperlinkJsonDeserialiser());

        this.module.addSerializer(RichText.class, new RichTextJsonSerialiser());
        this.module.addDeserializer(RichText.class, new RichTextJsonDeserialiser(this));

        this.module.addSerializer(Result.class, new ResultJsonSerialiser(this));
        this.module.addSerializer(NeedMoreDataException.class, new NeedMoreDataExceptionJsonSerialiser(this));
        this.module.addDeserializer(Result.class, new ResultJsonDeserialiser(this));

        this.module.addDeserializer(ArrayList.class, new ArrayListJsonDeserialiser(this, serialisationTypeEncoder));
        this.module.addDeserializer((Class<List>) ClassesRetriever.findClass("java.util.Arrays$ArrayList"), new ArraysArrayListJsonDeserialiser(this, serialisationTypeEncoder));

        registerModule(module);
        
        // the following is required strictly for the use in clearCaches()
        // EXPERIMENTAL
//        try {
//            final Field field = getDeserializationConfig().getClassIntrospector().getClass().getDeclaredField("_cachedFCA");
//            field.setAccessible(true);
//            cachedFCAsToClear = (LRUMap<?,?>) field.get(getDeserializationConfig().getClassIntrospector());
//        } catch (final Exception ex) {
//            throw new SerialisationException("Could obtain a field referene to _cachedFCA.", ex);
//        }

    }

    /**
     * Register all serialisers / deserialisers for entity types present in TG app.
     */
    protected void registerEntityTypes(final ISerialisationClassProvider provider, final TgJacksonModule module) {
        new EntitySerialiser<EntityType>(EntityType.class, this.module, this, this.factory, entityTypeInfoGetter, true, serialisationTypeEncoder, idOnlyProxiedEntityTypeCache, false);
        new EntitySerialiser<EntityTypeProp>(EntityTypeProp.class, this.module, this, this.factory, entityTypeInfoGetter, true, serialisationTypeEncoder, idOnlyProxiedEntityTypeCache, false);
        for (final Class<?> type : provider.classes()) {
            if (EntityUtils.isPropertyDescriptor(type)) {
                new EntitySerialiser<PropertyDescriptor<?>>((Class<PropertyDescriptor<?>>) ClassesRetriever.findClass("ua.com.fielden.platform.entity.meta.PropertyDescriptor"), this.module, this, this.factory, entityTypeInfoGetter, false, serialisationTypeEncoder, idOnlyProxiedEntityTypeCache, true);
            } else if (AbstractEntity.class.isAssignableFrom(type) && !UserSecret.class.isAssignableFrom(type)) {
                new EntitySerialiser<AbstractEntity<?>>((Class<AbstractEntity<?>>) type, module, this, factory, entityTypeInfoGetter, serialisationTypeEncoder, idOnlyProxiedEntityTypeCache);
            }
        }
    }

    /// This is very much an experimental attempt to remedy accumulation of generated types inside Jackson caches.
    ///
    /// Note that the original name of this method was `clearCaches` (`s` at the end), but since the update to Jackson `2.19.2` it started clashing with the method [ObjectMapper#clearCaches()].
    private void clearCache() {
        TypeFactory.defaultInstance().clearCache();
        // flushing cache is a synchronized operation
        final DefaultSerializerProvider defaultSerializerProvider = (DefaultSerializerProvider) getSerializerProvider();
        defaultSerializerProvider.flushCachedSerializers();
    }
    
    /**
     * Registers the new type and returns the [number; EntityType].
     *
     * @param newType
     * @return
     */
    public EntityType registerNewEntityType(final Class<AbstractEntity<?>> newType) {
        clearCache();
        return new EntitySerialiser<AbstractEntity<?>>(newType, module, this, factory, entityTypeInfoGetter, serialisationTypeEncoder, idOnlyProxiedEntityTypeCache).getEntityTypeInfo();
    }
    
    @Override
    public <T> T deserialise(final byte[] content, final Class<T> type) {
        final ByteArrayInputStream bis = new ByteArrayInputStream(content);
        return deserialise(bis, type);
    }

    @Override
    public <T> T deserialise(final InputStream content, final Class<T> type) {
        if (UserSecret.class.isAssignableFrom(type)) {
            throw new EntityDeserialisationException(format(ERR_RESTRICTED_TYPE_DESERIALISATION, type.getSimpleName()));
        }
        
        try {
            final String contentString = IOUtils.toString(content, "UTF-8");
            logger.debug("JSON before deserialisation = |" + contentString + "|.");

            final JavaType concreteType;
            if (EntityUtils.isEntityType(type)) {
                concreteType = extractConcreteType(constructUnsafe(type), () -> {
                    try {
                        EntitySerialiser.getContext().reset();
                        final JsonNode treeNode = readTree(contentString);
                        return treeNode.get("@id") == null ? treeNode.get("@id_ref") : treeNode.get("@id");
                    } catch (final IOException e) {
                        logger.error(e.getMessage(), e);
                        throw new SerialisationException(format("Could not construct JavaType during deserialisation of [%s].", type.getName()), e);
                    }
                }, getTypeFactory(), serialisationTypeEncoder);

            } else {
                concreteType = getTypeFactory().constructType(type);
            }
            EntitySerialiser.getContext().reset();
            return readValue(contentString, concreteType);
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
            throw new SerialisationException(format("Error during deserialisation of [%s].", type.getName()), e);
        }
    }

    /**
     * Extracts concrete type for 'type' in case whether the 'type' is entity and abstract.
     *
     * @param idNodeSupplier
     *            -- the supplier function to retrieve idNode
     * @param typeFactory
     * @return
     */
    public static JavaType extractConcreteType(final ResolvedType type, final Supplier<JsonNode> idNodeSupplier, final TypeFactory typeFactory, final ISerialisationTypeEncoder serialisationTypeEncoder) {
        if (EntityUtils.isEntityType(type.getRawClass()) && Modifier.isAbstract(type.getRawClass().getModifiers())) {
            // when we are trying to deserialise an entity of unknown concrete type (e.g. passing AbstractEntity.class) -- there is a need to determine concrete type from @id property
            final JsonNode idNode = idNodeSupplier.get();
            if (idNode != null && !idNode.isNull()) {
                final String entityTypeId = idNode.asText().split("#")[0];
                final Class<?> decodedType = serialisationTypeEncoder.decode(entityTypeId);
                return constructUnsafe(decodedType);
            } else {
                return (JavaType) type;
            }
        } else {
            return (JavaType) type;
        }
    }

    @Override
    public byte[] serialise(final Object obj) {
        if (obj instanceof UserSecret) {
            throw new EntitySerialisationException(format(ERR_RESTRICTED_TYPE_SERIALISATION, obj.getClass().getSimpleName()));
        }

        try {
            // logger.debug("Serialised pretty JSON = |" + new String(writerWithDefaultPrettyPrinter().writeValueAsBytes(obj), Charsets.UTF_8) + "|.");
            EntitySerialiser.getContext().reset();
            final byte[] bytes = writeValueAsBytes(obj); // default encoding is Charsets.UTF_8
            logger.debug("Serialised JSON = |" + new String(bytes, Charsets.UTF_8) + "|.");

            return bytes;
        } catch (final JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            throw new SerialisationException("Serialisation failed.", e);
        }
    }

    @Override
    public EntityFactory factory() {
        return factory;
    }

    public Map<String, EntityType> getTypeTable() {
        return entityTypeInfoGetter.getTypeTable();
    }
    
    public EntityTypeInfoGetter getEntityTypeInfoGetter() {
        return entityTypeInfoGetter;
    }
}
