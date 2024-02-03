package ua.com.fielden.platform.entity.query.metadata;

import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;

public class PureEntityMetadata<ET extends AbstractEntity<?>> extends AbstractEntityMetadata<ET>{

    public PureEntityMetadata(final Class<ET> type) {
        super(type, new TreeMap<>());
    }
}