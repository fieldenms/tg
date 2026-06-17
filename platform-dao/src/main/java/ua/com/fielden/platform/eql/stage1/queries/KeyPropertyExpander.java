package ua.com.fielden.platform.eql.stage1.queries;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForEntityType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.Component;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.CompositeKey;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.Entity;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.Optional;
import java.util.stream.Stream;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.utils.CollectionUtil.concatList;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

final class KeyPropertyExpander {

    private KeyPropertyExpander() {}

    /// If `prop` represents a path that ends with `key`, the result is a stream of paths, each of which replaces `key` by a specific key member.
    /// Otherwise, the result is a stream that contains only `prop`.
    ///
    public static Stream<Prop2> expand(final Prop2 prop, final IDomainMetadata domainMetadata) {
        if (isExpandable(prop.lastPart(), prop.penultPart())) {
            final int pathSize = prop.getPath().size();
            final var querySourceInfo = pathSize == 1
                    ? prop.source.querySourceInfo()
                    : ((QuerySourceItemForEntityType<?>) prop.getPath().get(pathSize - 2)).querySourceInfo;
            return keyPaths(querySourceInfo.javaType(), domainMetadata)
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

    /// This method is an alternative to [EntityUtils#keyPaths], implemented using [IDomainMetadata].
    /// Once IoC is better harnessed, this method should become the definitive one.
    ///
    private static Stream<String> keyPaths(final Class<? extends AbstractEntity<?>> entityType, final IDomainMetadata domainMetadata) {
        final var mdEntity = domainMetadata.forEntity(entityType);
        return switch (mdEntity.property(KEY).type()) {
            case CompositeKey _ ->
                    domainMetadata.entityMetadataUtils().compositeKeyMembers(mdEntity)
                            .stream()
                            .flatMap(keyMember -> keyMemberPaths(keyMember, domainMetadata)
                                                  .map(subs -> subs.map(s -> keyMember.name() + "." + s))
                                                  .orElseGet(() -> Stream.of(keyMember.name())));
            // Simple entity-typed key.
            case Entity et  -> keyPaths(et.javaType(), domainMetadata).map(path -> KEY + "." + path);
            default -> Stream.of(KEY);
        };
    }

    private static Optional<Stream<String>> keyMemberPaths(final PropertyMetadata keyMember, final IDomainMetadata domainMetadata) {
        return switch (keyMember.type()) {
            case Entity it when domainMetadata.forEntity(it.javaType()).isUnion() -> Optional.of(Stream.of(KEY));
            // NOTE It is unclear why only persistent entity types are expanded.
            //      This is in place to align with EntityUtils.keyPaths.
            case Entity it when domainMetadata.forEntity(it.javaType()).isPersistent() -> Optional.of(keyPaths(it.javaType(), domainMetadata));
            case Component _ -> Optional.of(domainMetadata.propertyMetadataUtils()
                                                     .subProperties(keyMember)
                                                     .stream()
                                                     .filter(PropertyMetadata::isPersistent)
                                                     .map(PropertyMetadata::name));
            default -> Optional.empty();
        };
    }

}
