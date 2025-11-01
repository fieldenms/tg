package ua.com.fielden.platform.eql.stage3.queries;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sundries.GroupBys3;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBys3;
import ua.com.fielden.platform.eql.stage3.sundries.Yields3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Predicates.or;
import static ua.com.fielden.platform.entity.query.DbVersion.ORACLE;
import static ua.com.fielden.platform.eql.meta.PropType.NULL_TYPE;

public abstract class AbstractQuery3 implements ToString.IFormattable {

    public final Optional<IJoinNode3> maybeJoinRoot;
    public final Conditions3 whereConditions;
    public final Yields3 yields;
    public final GroupBys3 groups;
    public final OrderBys3 orderings;
    public final Class<?> resultType;

    public AbstractQuery3(final QueryComponents3 queryComponents, final Class<?> resultType) {
        this.maybeJoinRoot = queryComponents.maybeJoinRoot();
        this.whereConditions = queryComponents.whereConditions();
        this.yields = queryComponents.yields();
        this.groups = queryComponents.groups();
        this.orderings = queryComponents.orderings();
        this.resultType = resultType;
    }

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return sql(metadata, dbVersion, Collections.nCopies(yields.getYields().size(), NULL_TYPE));
    }

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion, final List<PropType> expectedYieldTypes) {
        final StringBuilder sb = new StringBuilder();
        sb.append(yields.sql(metadata, dbVersion, expectedYieldTypes));
        sb.append(maybeJoinRoot.map(joinRoot -> "\nFROM\n" + joinRoot.sql(metadata, dbVersion))
                          .orElseGet(() -> dbVersion == ORACLE ? " FROM DUAL " : ""));
        sb.append(whereConditions != null ? "\nWHERE " + whereConditions.sql(metadata, dbVersion) : "");
        sb.append(groups != null ? "\nGROUP BY " + groups.sql(metadata, dbVersion) : "");
        sb.append(orderings != null ? "\nORDER BY " + orderings.sql(metadata, dbVersion, this) : "");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(yields, orderings, groups, whereConditions, maybeJoinRoot, resultType);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof AbstractQuery3 that
                  && Objects.equals(maybeJoinRoot, that.maybeJoinRoot)
                  && Objects.equals(yields, that.yields)
                  && Objects.equals(whereConditions, that.whereConditions)
                  && Objects.equals(groups, that.groups)
                  && Objects.equals(orderings, that.orderings)
                  && Objects.equals(resultType, that.resultType);
    }

    public static boolean isTopLevelQuery(final AbstractQuery3 query) {
        return query instanceof ResultQuery3;
    }

    public static boolean isSubQuery(final AbstractQuery3 query) {
        return !isTopLevelQuery(query);
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("resultType", resultType)
                .addIfPresent("join", maybeJoinRoot)
                .addIfNot("where", whereConditions, or(Objects::isNull, Conditions3::isEmpty))
                .addIfNot("yields", yields, or(Objects::isNull, Yields3::isEmpty))
                .addIfNot("groups", groups, or(Objects::isNull, GroupBys3::isEmpty))
                .addIfNot("orderings", orderings, or(Objects::isNull, OrderBys3::isEmpty))
                .pipe(this::addToString)
                .$();
    }

    protected ToString addToString(final ToString toString) {
        return toString;
    }

}
