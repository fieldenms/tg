package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractUnionEntity;

import javax.annotation.Nullable;

public interface UnionMatchable<U extends AbstractUnionEntity, M> {

    static <U extends AbstractUnionEntity, M> @Nullable M match(@Nullable UnionMatchable<U, M> union) {
        // TODO Analyse the union entity type to generate the matching procedure.
        return null;
    }

}
