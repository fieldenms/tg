package ua.com.fielden.platform.persistence.types;

import com.google.common.collect.ImmutableMap;
import com.google.inject.ImplementedBy;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;

import java.util.Map;
import java.util.Optional;

/// Establishes mappings between:
/// * Property types and Hibernate types.
/// * Interfaces for Hibernate types and their implementations: [IUserTypeInstantiate] and [ICompositeUserTypeInstantiate].
///
/// A Hibernate type is an instance of either [UserType], [Type] or [CompositeUserType].
///
/// For each method that returns an optional result, the result will be empty if the supplied type has no corresponding
/// registered Hibernate type.
///
/// The platform provides standard mappings represented by [PlatformHibernateTypeMappingsProvider].
///
/// Applications can provide their own mappings by implementing [Provider] and binding it in an IoC module.
///
/// {@snippet :
/// class MyProvider implements HibernateTypeMappings.Provider {
///     PlatformHibernateTypeMappingsProvider platformProvider;
///
///     @Inject
///     protected MyProvider(PlatformHibernateTypeMappingsProvider platformProvider) {
///         this.platformProvider = platformProvider;
///     }
///
///     @Override
///     public HibernateTypeMappings get() {
///         return HibernateTypeMappings.builder(platformProvider.get())
///                 .put(Money.class, MoneyType.INSTANCE)
///                 .build();
///     }
/// }
///
public interface HibernateTypeMappings {

    /// Given a subtype of [IUserTypeInstantiate] or [ICompositeUserTypeInstantiate], returns an instance of its implementation.
    /// Given any other type, returns the corresponding Hibernate type.
    ///
    Optional<Object> getHibernateType(Class<?> type);

    Map<Class<?>, Object> allMappings();

    static HibernateTypeMappings empty() {
        return new HibernateTypeMappingsImpl(ImmutableMap.of());
    }

    static HibernateTypeMappings of(Map<? extends Class<?>, Object> map) {
        return new HibernateTypeMappingsImpl(map);
    }

    static HibernateTypeMappingsBuilder builder() {
        return new HibernateTypeMappingsBuilder();
    }

    static HibernateTypeMappingsBuilder builder(HibernateTypeMappings mappings) {
        return new HibernateTypeMappingsBuilder(mappings);
    }

    @ImplementedBy(PlatformHibernateTypeMappingsProvider.class)
    interface Provider {
        HibernateTypeMappings get();
    }

}
