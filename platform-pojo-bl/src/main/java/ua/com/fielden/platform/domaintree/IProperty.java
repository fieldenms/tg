package ua.com.fielden.platform.domaintree;

/// Represents an entity property.
///
/// [#getRoot] together with [#path] identify the property's location.
///
public interface IProperty {

    /// The property's root type -- the type that declares the property.
    ///
    Class<?> getRoot();

    /// The property's context path.
    ///
    String getContextPath();

    /// The property's title.
    ///
    String getTitle();

    /// The property's description.
    ///
    String getDesc();

    /// The property's name.
    ///
    String name();

    /// The property's type.
    ///
    Class<?> resultType();

    /// The path where the property exists in the root type [#getRoot].
    /// It can be different from [#getContextPath].
    ///
    String path();
    
    Integer getPrecision();
    
    Integer getScale();
}
