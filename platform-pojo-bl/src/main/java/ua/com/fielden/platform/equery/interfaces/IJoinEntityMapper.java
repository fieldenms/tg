package ua.com.fielden.platform.equery.interfaces;

import ua.com.fielden.platform.equery.tokens.properties.PropertyOrigin;

public interface IJoinEntityMapper extends IEntityMapper {
    IEntityMapper getParentMapperForEntityPropertyInSelect(final String propName);
    String getAliasedProperty(final PropertyOrigin propertyOrigin, final String propertyDotName);
}
