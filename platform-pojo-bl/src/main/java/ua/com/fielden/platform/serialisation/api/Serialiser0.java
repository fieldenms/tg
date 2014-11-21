package ua.com.fielden.platform.serialisation.api;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.kryo.TgKryo0;

import com.google.inject.Inject;

/**
 * The default implementation for {@link ISerialiser0} with two engines: KRYO (default) and JACKSON.
 *
 * @author TG Team
 *
 */
@Deprecated
public class Serialiser0 extends Serialiser implements ISerialiser0 {

    @Inject
    @Deprecated
    public Serialiser0(final EntityFactory factory, final ISerialisationClassProvider provider) {
        super(factory, provider);
    }

    @Override
    protected ISerialiserEngine createTgKryo(final EntityFactory factory, final ISerialisationClassProvider provider) {
        return new TgKryo0(factory, provider, this);
    }

}
