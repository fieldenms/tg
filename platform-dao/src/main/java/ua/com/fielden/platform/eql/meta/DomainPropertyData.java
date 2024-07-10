package ua.com.fielden.platform.eql.meta;

public final class DomainPropertyData {
    public final long id;
    public final String name;
    public final Long holderAsDomainType;
    public final Long holderAsDomainProperty;
    public final long domainType;
    public final String title;
    public final String desc;
    public final Integer keyIndex;
    public final boolean required;
    public final String dbColumn;
    public final int position;

    public DomainPropertyData(final long id, final String name, final Long holderAsDomainType, final Long holderAsDomainProperty, final long domainType, final String title, final String desc, final Integer keyIndex, final boolean required, final String dbColumn, final int position) {
        this.id = id;
        this.name = name;
        this.holderAsDomainType = holderAsDomainType;
        this.holderAsDomainProperty = holderAsDomainProperty;
        this.domainType = domainType;
        this.title = title;
        this.desc = desc;
        this.keyIndex = keyIndex;
        this.required = required;
        this.dbColumn = dbColumn;
        this.position = position;
    }

}
