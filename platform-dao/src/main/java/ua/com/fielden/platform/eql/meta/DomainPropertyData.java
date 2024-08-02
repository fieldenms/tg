package ua.com.fielden.platform.eql.meta;

record DomainPropertyData(
        long id,
        String name,
        DomainTypeData holderAsDomainType,
        DomainPropertyData holderAsDomainProperty,
        DomainTypeData domainType,
        String title,
        String desc,
        Integer keyIndex,
        boolean required,
        String dbColumn,
        int position)
{}
