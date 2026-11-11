package ua.com.fielden.platform.web_api;

import ua.com.fielden.platform.entity.AbstractEntity;

/// An entity type as exposed by the Web API: its GraphQL type name together with the Java type that it represents.
///
/// For a domain type, the name is the simple name of the Java type.
///
/// @param name  the GraphQL type name, from which the names of all GraphQL definitions for this entity type are derived
/// @param type  the Java type, which properties and queries are based on
///
public record WebApiEntityType(String name, Class<? extends AbstractEntity<?>> type) {

    public static WebApiEntityType domainType(final Class<? extends AbstractEntity<?>> type) {
        return new WebApiEntityType(type.getSimpleName(), type);
    }

}
