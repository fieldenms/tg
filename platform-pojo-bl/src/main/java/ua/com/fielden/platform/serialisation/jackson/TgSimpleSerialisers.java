package ua.com.fielden.platform.serialisation.jackson;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.stripIfNeeded;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.exceptions.SerialisationException;

public class TgSimpleSerialisers extends SimpleSerializers {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TgSimpleSerialisers.class);

    private final transient TgJacksonModule module;
    public final transient Cache<Class<?>, JsonSerializer<?>> genClassSerialisers = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).initialCapacity(1000).build();

    public TgSimpleSerialisers(final TgJacksonModule module) {
        this.module = module;
    }
    
    @Override
    protected void _addSerializer(final Class<?> type, final JsonSerializer<?> ser) {
        if (DynamicEntityClassLoader.isGenerated(type)) {
            genClassSerialisers.put(type, ser);
        } else {
            super._addSerializer(type, ser);
        }
    }

    @Override
    public JsonSerializer<?> findSerializer(final SerializationConfig config, final JavaType type, final BeanDescription beanDesc) {
        final Class<?> klass = stripIfNeeded(type.getRawClass());
        if (DynamicEntityClassLoader.isGenerated(klass)) {
            try {
                return genClassSerialisers.get(klass, () -> {
                    module.getObjectMapper().registerNewEntityType((Class<AbstractEntity<?>>) klass);
                    return genClassSerialisers.getIfPresent(klass);
                });
            } catch (final ExecutionException ex) {
                LOGGER.error(ex);
                throw new SerialisationException(format("Could not determine a serialiser for type [%s].", klass.getName()), ex);
            }
        }
        
        return super.findSerializer(config, type, beanDesc);
    }

}
