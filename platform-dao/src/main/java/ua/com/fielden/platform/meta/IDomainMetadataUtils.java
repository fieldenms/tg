package ua.com.fielden.platform.meta;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;

import java.util.stream.Stream;

/**
 * Utilities related to domain metadata.
 *
 * @see IDomainMetadata
 */
@ImplementedBy(DomainMetadataUtils.class)
public interface IDomainMetadataUtils {

    /**
     * Returns a stream of metadata for all registered entity types, as specified by {@link IApplicationDomainProvider}.
     * Entity types that do not have metadata are excluded.
     *
     * @see EntityMetadata
     */
    Stream<EntityMetadata> registeredEntities();

}
