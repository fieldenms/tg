package ua.com.fielden.platform.serialisation.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;

/**
 * {@link SimpleModule} for TG serialisation with {@link SerialiserEngines#JACKSON} serialisation engine.
 *
 * @author TG Team
 *
 */
public class TgJacksonModule extends SimpleModule {
    private static final long serialVersionUID = -2741883920547263539L;

    private final TgJackson objectMapper;

    public TgJacksonModule(final TgJackson objectMapper) {
        super("Tg Jackson module", new Version(1, 0, 0, null, null, null));
        this.objectMapper = objectMapper;
        setSerializers(new TgSimpleSerialisers(this));
        setDeserializers(new TgSimpleDeserialisers(this));
    }

    public TgJackson getObjectMapper() {
        return objectMapper;
    }

}
