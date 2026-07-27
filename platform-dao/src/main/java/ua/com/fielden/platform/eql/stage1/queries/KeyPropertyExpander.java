package ua.com.fielden.platform.eql.stage1.queries;

import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForEntityType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;

import java.util.Optional;
import java.util.stream.Stream;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.utils.CollectionUtil.concatList;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.keyPaths;

final class KeyPropertyExpander {

    private KeyPropertyExpander() {}

    /// If `prop` represents a path that ends with `key`, the result is a stream of paths, each of which replaces `key` by a specific key member.
    /// Otherwise, the result is a stream that contains only `prop`.
    ///
    public static Stream<Prop2> expand(final Prop2 prop) {
        if (isExpandable(prop.lastPart(), prop.penultPart())) {
            final int pathSize = prop.getPath().size();
            final var querySourceInfo = pathSize == 1
                    ? prop.source.querySourceInfo()
                    : ((QuerySourceItemForEntityType<?>) prop.getPath().get(pathSize - 2)).querySourceInfo;
            return keyPaths(querySourceInfo.javaType())
                    .stream()
                    .map(keyPath -> {
                        final var resolution = querySourceInfo.resolve(new PropResolutionProgress(keyPath));
                        return new Prop2(prop.source, concatList(prop.getPath().subList(0, pathSize - 1), resolution.getResolved()));
                    });
        }
        else {
            return Stream.of(prop);
        }
    }

    public static boolean isExpandable(final AbstractQuerySourceItem<?> lastItem, final Optional<AbstractQuerySourceItem<?>> maybePenultItem) {
        // TODO Support extraction of key members in union entities.
        //      For now, we ignore union entities, and the effect is that `union.key` will be used -- a string representation --
        //      instead of having access to each key member individually.
        final var caseOfUnionEntity = maybePenultItem.filter(item -> item instanceof QuerySourceItemForUnionType).isPresent();
        return !caseOfUnionEntity
               && KEY.equals(lastItem.name)
               && (lastItem.hasExpression()/*composite*/ || isEntityType(lastItem.javaType())/*1-2-1*/);
    }

}
