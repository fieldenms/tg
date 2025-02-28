package ua.com.fielden.platform.meta;

import com.google.inject.Inject;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;

import java.util.stream.Stream;

// TODO: Remove 'public' once dependent tests are rationalised with IoC.
public final class DomainMetadataUtils implements IDomainMetadataUtils {

    private final IApplicationDomainProvider appDomain;
    private final IDomainMetadata domainMetadata;

    /**
     * <b> Do not use this constructor. Use IoC instead. </b>
     * This constructor remains public to suppport platform tests that have not yet been rationalised with IoC.
     */
    @Inject
    public DomainMetadataUtils(final IApplicationDomainProvider appDomain, final IDomainMetadata domainMetadata) {
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
