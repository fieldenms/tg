package ua.com.fielden.platform.eql.retrieval;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.List;

import org.hibernate.type.Type;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;
import ua.com.fielden.platform.eql.retrieval.records.EntityTree;
import ua.com.fielden.platform.eql.retrieval.records.HibernateScalar;
import ua.com.fielden.platform.eql.retrieval.records.QueryResultLeaf;
import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.persistence.types.UtcDateTimeType;
import ua.com.fielden.platform.sample.domain.TgAuthor;

public class EntityResultTreeBuilderTest extends AbstractEqlShortcutTest {

    @Test
    public void yielding_date_props_into_entity_aggregates_preserves_original_hib_types() {
        final EntityTree<EntityAggregates> act = buildResultTree(select(TgAuthor.class).
                yield().prop("dob").as("dob").
                yield().prop("utcDob").as("utcDob").
                modelAsAggregate());
        
        final List<QueryResultLeaf> leaves = List.of(
                qrl(0, "dob", "C_2", DateTimeType.INSTANCE),
                qrl(1, "utcDob", "C_3", UtcDateTimeType.INSTANCE, UtcDateTimeType.INSTANCE));
        
        final EntityTree<EntityAggregates> exp = new EntityTree<>(EntityAggregates.class, leaves, emptyMap(), emptyMap());
        assertEquals(exp, act);
    }  

    private static QueryResultLeaf qrl(final int position, final String name, final String column, final Type hibType) {
        return new QueryResultLeaf(position, name, new HibernateScalar(column, hibType), null);
    }
    
    private static QueryResultLeaf qrl(final int position, final String name, final String column, final IUserTypeInstantiate hibUserType) {
        return new QueryResultLeaf(position, name, new HibernateScalar(column, null), hibUserType);
    }
    
    private static QueryResultLeaf qrl(final int position, final String name, final String column, final Type hibType, final IUserTypeInstantiate hibUserType) {
        return new QueryResultLeaf(position, name, new HibernateScalar(column, hibType), hibUserType);
    }
}