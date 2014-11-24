package ua.com.fielden.platform.serialisation.jackson;

import ua.com.fielden.platform.serialisation.api.SerialiserEngines;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * {@link SimpleModule} for TG serialisation with {@link SerialiserEngines#JACKSON} serialisation engine.
 *
 * @author TG Team
 *
 */
public class TgJacksonModule extends SimpleModule {
    private static final long serialVersionUID = -2741883920547263539L;

    public TgJacksonModule() {
        super("Tg Jackson module", new Version(1, 0, 0, null, null, null));
    }
}
