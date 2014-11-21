package ua.com.fielden.platform.serialisation.kryo;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;

import com.google.inject.Inject;

/**
 *
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
