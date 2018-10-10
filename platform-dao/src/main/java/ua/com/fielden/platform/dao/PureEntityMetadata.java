package ua.com.fielden.platform.dao;

import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;

public class PureEntityMetadata<ET extends AbstractEntity<?>> extends AbstractEntityMetadata<ET>{

    public PureEntityMetadata(final String table, final Class<ET> type) {
        super(type, new TreeMap<>());
    }
}