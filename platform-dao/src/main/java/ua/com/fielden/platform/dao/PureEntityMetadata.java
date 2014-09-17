package ua.com.fielden.platform.dao;

import java.util.SortedMap;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;

public class PureEntityMetadata<ET extends AbstractEntity<?>> extends AbstractEntityMetadata<ET>{

        public PureEntityMetadata(final String table, final Class<ET> type, final SortedMap<String, PropertyMetadata> props) {
        super(type, props);
    }
}