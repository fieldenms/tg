package ua.com.fielden.platform.web_api;

import com.google.common.collect.ImmutableMap;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Map;

/// An entity type defined at runtime, which extends a domain type with calculated properties.
///
/// Its Java type is a subclass of the base type, generated and named as all types with calculated properties added at runtime are,
/// or the base type itself if there are no added properties.
/// The Web API exposes it under `name` instead.
///
/// @param name        the name of this type in the Web API
/// @param baseType    the domain type that this type extends
/// @param properties  expressions of the added calculated properties by property name
/// @param type        the Java type
///
public record EnhancedEntityType(
        String name,
        Class<? extends AbstractEntity<?>> baseType,
        Map<String, String> properties,
        Class<? extends AbstractEntity<?>> type)
{

    public EnhancedEntityType {
        properties = ImmutableMap.copyOf(properties);
    }

    public WebApiEntityType webApiEntityType() {
        return new WebApiEntityType(name, type, baseType);
    }

}
