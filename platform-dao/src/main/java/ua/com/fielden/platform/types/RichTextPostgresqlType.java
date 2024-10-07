package ua.com.fielden.platform.types;

import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import ua.com.fielden.platform.entity.query.DbVersion;

/**
 * Custom Hibernate type for {@link RichText}, designed specifically for PostgreSQL.
 * <p>
 * Users of this class should not instantiate it directly, but use {@link RichTextType#getInstance(DbVersion)}.
 * The constructor is made public to satisfy the requirements for Hibernate custom types.
 */
public final class RichTextPostgresqlType extends RichTextType {

    static final RichTextPostgresqlType INSTANCE = new RichTextPostgresqlType();

    /**
     * <b>Do not use this constructor directly</b>! Use {@link RichTextType#getInstance(DbVersion)}.
     */
    public RichTextPostgresqlType() {
        super(StringType.INSTANCE);
    }

}
