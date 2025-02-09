package ua.com.fielden.platform.eql.stage3.queries;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;
import ua.com.fielden.platform.eql.stage3.sundries.GroupBys3;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBys3;
import ua.com.fielden.platform.eql.stage3.sundries.Yield3;
import ua.com.fielden.platform.eql.stage3.sundries.Yields3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static ua.com.fielden.platform.entity.query.DbVersion.ORACLE;

public abstract class AbstractQuery3 {

    public final IJoinNode3 joinRoot;
    public final Conditions3 whereConditions;
    public final Yields3 yields;
    public final GroupBys3 groups;
    public final OrderBys3 orderings;
    public final Class<?> resultType;

    public AbstractQuery3(final QueryComponents3 queryComponents, final Class<?> resultType) {
        this.joinRoot = queryComponents.joinRoot();
        this.whereConditions = queryComponents.whereConditions();
        this.yields = queryComponents.yields();
        this.groups = queryComponents.groups();
        this.orderings = queryComponents.orderings();
        this.resultType = resultType;
    }

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return sql(metadata, dbVersion, Collections.nCopies(yields.getYields().size(), Yield3.NO_EXPECTED_TYPE));
    }

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion, final List<PropType> expectedYieldTypes) {
        final StringBuffer sb = new StringBuffer();
        sb.append(yields.sql(metadata, dbVersion, expectedYieldTypes));
        sb.append(joinRoot != null ? "\nFROM\n" + joinRoot.sql(metadata, dbVersion) : (dbVersion == ORACLE ? " FROM DUAL " : ""));
        sb.append(whereConditions != null ? "\nWHERE " + whereConditions.sql(metadata, dbVersion) : "");
        sb.append(groups != null ? "\nGROUP BY " + groups.sql(metadata, dbVersion) : "");
        sb.append(orderings != null ? "\nORDER BY " + orderings.sql(metadata, dbVersion, this) : "");
        return sb.toString();
    }

    @Override
    public String toString() {
        // TODO choose an informative representation
//        return sql(DbVersion.H2);
        return super.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(yields, orderings, groups, whereConditions, joinRoot, resultType);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof AbstractQuery3 that
                  && Objects.equals(joinRoot, that.joinRoot)
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

}
