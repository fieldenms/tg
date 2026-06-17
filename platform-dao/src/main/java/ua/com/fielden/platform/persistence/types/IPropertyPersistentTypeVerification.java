package ua.com.fielden.platform.persistence.types;

import com.google.inject.ImplementedBy;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.meta.IDomainMetadataUtils;

import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.utils.StreamUtils.enumerate;

/// A contract that describes verification of persistent property type configuration.
/// Each property may have a custom persistent type specified via annotation [PersistentType].
/// There is also the global configuration [HibernateTypeMappings].
///
/// Available implementations:
/// * [StrictPropertyPersistentTypeVerification]
/// * [NoPropertyPersistentTypeVerification]
///
@ImplementedBy(StrictPropertyPersistentTypeVerification.class)
public interface IPropertyPersistentTypeVerification {

    /// Verifies an entity property.
    /// Implementations need not specify entity and property name in the result message.
    ///
    Result verify(Class<? extends AbstractEntity<?>> entityType, CharSequence property);

    /// A service that runs the verification.
    /// The platform requests static injection of this class in an IoC module to run on application startup.
    ///
    class Service {

        static final Logger LOGGER = getLogger();

        @Inject
        static void start(
                final IPropertyPersistentTypeVerification verification,
                final IDomainMetadataUtils domainMetadataUtils)
        {
            record R (Class<? extends AbstractEntity<?>> entityType, String property, Result result) {
                String toMessage() {
                    return "%s.%s: %s".formatted(entityType.getSimpleName(), property, result.getMessage());
                }
            }

            final var results = domainMetadataUtils.registeredEntities()
                    .flatMap(mdEntity -> mdEntity.properties()
                            .stream()
                            .map(mdProp -> new R(mdEntity.javaType(), mdProp.name(), verification.verify(mdEntity.javaType(), mdProp.name()))))
                    .collect(groupingBy(r -> {
                        if (!r.result.isSuccessful()) {
                            return "fail";
                        }
                        else if (r.result.isWarning()) {
                            return "warn";
                        }
                        else if (r.result.isInformative()) {
                            return "info";
                        }
                        else return "success";
                    }));

            results.getOrDefault("warn", List.of()).stream().map(R::toMessage).forEach(LOGGER::warn);
            results.getOrDefault("info", List.of()).stream().map(R::toMessage).forEach(LOGGER::info);
            final var failures = results.getOrDefault("fail", List.of());
            if (!failures.isEmpty()) {
                final var failuresMsg = enumerate(failures.stream(),
                                                  1,
                                                  (r, i) -> "%s. %s".formatted(i, r.toMessage()))
                        .collect(joining("\n"));
                throw new EntityDefinitionException(format(
                        """
                        Verification of @%s failed for some domain entities.
                        %s""",
                        PersistentType.class.getSimpleName(),
                        failuresMsg));
            }
        }
    }

}
