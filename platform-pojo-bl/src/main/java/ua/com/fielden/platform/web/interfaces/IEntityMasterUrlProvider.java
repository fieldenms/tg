package ua.com.fielden.platform.web.interfaces;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Optional;

/// A contract to generate an entity master URI for a specific entity.
///
/// An implementation should be bound in an IoC module.
/// The default implementation doesn't provide meaningful results.
///
@FunctionalInterface
@ImplementedBy(StubEntityMasterUrlProvider.class)
public interface IEntityMasterUrlProvider {

    String PARTIAL_URL_PATTERN = "#/master/%s/%s";

    /// Generates an entity master URI for `entity`.
    /// It might return empty URI string if `entity` does not have a registered entity master.
    ///
    Optional<String> masterUrlFor(final AbstractEntity<?> entity);

}
