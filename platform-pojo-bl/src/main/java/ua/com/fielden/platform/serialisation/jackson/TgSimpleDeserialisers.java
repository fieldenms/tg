package ua.com.fielden.platform.serialisation.jackson;

import static java.lang.String.format;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.exceptions.SerialisationException;

import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.stripIfNeeded;

public class TgSimpleDeserialisers extends SimpleDeserializers {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TgSimpleDeserialisers.class);

    private final transient TgJacksonModule module;
    private final transient Cache<Class<?>,JsonDeserializer<?>> genClassDeserialisers = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).initialCapacity(1000).build();

    public TgSimpleDeserialisers(final TgJacksonModule module) {
        this.module = module;
    }

    @Override
    public <T> void addDeserializer(final Class<T> type, final JsonDeserializer<? extends T> deser) {
        if (DynamicEntityClassLoader.isGenerated(type)) {
            genClassDeserialisers.put(type, deser);
        } else {
            super.addDeserializer(type, deser);
        }

    }
    
    @Override
    public JsonDeserializer<?> findBeanDeserializer(final JavaType type, final DeserializationConfig config, final BeanDescription beanDesc) throws JsonMappingException {
        final Class<?> klass = stripIfNeeded(type.getRawClass());
        if (DynamicEntityClassLoader.isGenerated(klass)) {
            try {
                return genClassDeserialisers.get(klass, () -> {
                    module.getObjectMapper().registerNewEntityType((Class<AbstractEntity<?>>) klass);
                    return genClassDeserialisers.getIfPresent(klass);
                });
            } catch (final ExecutionException ex) {
                LOGGER.error(ex);
                throw new SerialisationException(format("Could not determine a serialiser for type [%s].", klass.getName()), ex);
            }
        }
        
        return super.findBeanDeserializer(type, config, beanDesc);
    }

}
