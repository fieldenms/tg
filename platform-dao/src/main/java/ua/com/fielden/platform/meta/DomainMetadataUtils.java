package ua.com.fielden.platform.meta;

import com.google.inject.Inject;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;

import java.util.stream.Stream;

final class DomainMetadataUtils implements IDomainMetadataUtils {

    private final IApplicationDomainProvider appDomain;
    private final IDomainMetadata domainMetadata;

    @Inject
    private DomainMetadataUtils(final IApplicationDomainProvider appDomain, final IDomainMetadata domainMetadata) {
        this.appDomain = appDomain;
        this.domainMetadata = domainMetadata;
    }

    @Override
    public Stream<EntityMetadata> registeredEntities() {
        return appDomain.entityTypes()
                .stream()
                .flatMap(ty -> domainMetadata.forEntityOpt(ty).stream());
    }

}
