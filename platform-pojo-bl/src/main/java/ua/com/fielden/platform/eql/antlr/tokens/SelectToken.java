// This file was generated. Timestamp: 2024-02-23T13:09:00.170947600+02:00[Europe/Kyiv]
package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public sealed abstract class SelectToken extends CommonToken {

    SelectToken() {
        super(EQLLexer.SELECT, "select");
    }

    public static EntityType entityType(final java.lang.Class<? extends AbstractEntity<?>> entityType) {
        return new EntityType(entityType);
    }

    public static Values values() {
        return Values.INSTANCE;
    }

    public static final class EntityType extends SelectToken {
        public final java.lang.Class<? extends AbstractEntity<?>> entityType;

        public EntityType(final java.lang.Class<? extends AbstractEntity<?>> entityType) {
            super();
            this.entityType = entityType;
        }
    }

    public static final class Values extends SelectToken {
        public static final Values INSTANCE = new Values();
    }

}
