package ua.com.fielden.platform.web.app;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;

public class SerialisationTypeEncoder implements ISerialisationTypeEncoder {

    @Override
    public <T extends AbstractEntity<?>> String encode(final Class<T> entityType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends AbstractEntity<?>> Class<T> decode(final String entityTypeId) {
        // TODO Auto-generated method stub
        return null;
    }

}
