package ua.com.fielden.platform.eql.meta;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.domain.metadata.DomainProperty;

/// An intermediate representation of [DomainProperty] used during generation.
///
/// @see DomainMetadataModelGenerator
///
record DomainPropertyData(
        long id,
        String name,
        @Nullable DomainTypeData holderAsDomainType,
        @Nullable DomainPropertyData holderAsDomainProperty,
        DomainTypeData domainType,
        String title,
        String desc,
        Integer keyIndex,
        boolean required,
        @Nullable String dbColumn,
        int position)
{}
