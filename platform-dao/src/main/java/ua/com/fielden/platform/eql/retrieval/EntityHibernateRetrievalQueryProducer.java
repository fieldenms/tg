package ua.com.fielden.platform.eql.retrieval;

import com.google.common.base.CharMatcher;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.type.StringNVarcharType;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;
import ua.com.fielden.platform.eql.retrieval.exceptions.EntityRetrievalException;
import ua.com.fielden.platform.eql.retrieval.records.HibernateScalar;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EntityHibernateRetrievalQueryProducer {
    //private static final Logger LOGGER = getLogger(EntityHibernateRetrievalQueryProducer.class);

    private EntityHibernateRetrievalQueryProducer() {}

    public static Query<?> produceQueryWithPagination(
            final Session session,
            final String sql,
            final List<HibernateScalar> retrievedColumns,
            final Map<String, Object> queryParams,
            final Integer pageNumber,
            final Integer pageCapacity,
            final DbVersion dbVersion)
    {
        // LOGGER.debug("\nSQL:\n   " + sql + "\n");
        final NativeQuery<?> sqlQuery = session.createNativeQuery(sql);
        specifyResultingFieldsToHibernateQuery(sqlQuery, retrievedColumns, dbVersion);
        specifyParamValuesToHibernateQuery(sqlQuery, queryParams, dbVersion);
        specifyPaginationToHibernateQuery(sqlQuery, pageNumber, pageCapacity);

        return sqlQuery.setReadOnly(true).setCacheable(false).setCacheMode(CacheMode.IGNORE);
    }

    public static Query<?> produceQueryWithoutPagination(
            final Session session,
            final String sql,
            final List<HibernateScalar> retrievedColumns,
            final Map<String, Object> queryParams,
            final DbVersion dbVersion)
    {
        return produceQueryWithPagination(session, sql, retrievedColumns, queryParams, null, null, dbVersion);
    }

    private static void specifyResultingFieldsToHibernateQuery(
            final NativeQuery<?> query,
            final List<HibernateScalar> retrievedColumns,
            final DbVersion dbVersion)
    {
        // If a column in the result set has type NVARCHAR, leave it up to SQL Server to determine its type (see Note 1 at the end of this file).
        final boolean skipNVarchar = (dbVersion == DbVersion.MSSQL);
        for (final HibernateScalar hibScalar : retrievedColumns) {
            if (skipNVarchar && hibScalar.hibType() == StringNVarcharType.INSTANCE) {
                query.addScalar(hibScalar.column());
            } else {
                query.addScalar(hibScalar.column(), hibScalar.hibType());
            }
            // LOGGER.debug("adding scalar: alias = [" + hibScalar.column() + "] type = [" + hibScalar.hibType() + "]");
        }
    }

    private static void specifyParamValuesToHibernateQuery(
            final NativeQuery<?> query,
            final Map<String, Object> queryParams,
            final DbVersion dbVersion)
    {
        // LOGGER.debug("\nPARAMS:\n   " + queryParams + "\n");
        for (final Map.Entry<String, Object> paramEntry : queryParams.entrySet()) {
            if (paramEntry.getValue() instanceof Collection) {
                throw new EntityRetrievalException("Should not have collectional param at this level: [" + paramEntry + "]");
            } else if (!(paramEntry.getValue() instanceof DynamicQueryBuilder.QueryProperty)){
                // UTF encoded (non-ASCII) String parameters need to be bound using the "national strings", usually represented by NVARCHAR type at the database level to preserve the encoding.
                // Remark on PostgreSQL:
                //    PostgreSQL JDBC drivers do not support {@code setNString(param, value)} and there is no type NVARCHAR at the database level.
                //    And so specifying type StringNVarcharType for PostgreSQL leads to a runtime error.
                //    Instead, the UTF encoding should be defined at the database level where it pertains to all VARCHAR columns.
                if (dbVersion != DbVersion.POSTGRESQL && paramEntry.getValue() instanceof String str && !CharMatcher.ascii().matchesAllOf(str)) {
                    query.setParameter(paramEntry.getKey(), str, StringNVarcharType.INSTANCE);
                }
                // Non-String, non-UTF string, and UTF string parameters in case of PostgreSQL
                else {
                    query.setParameter(paramEntry.getKey(), paramEntry.getValue());
                }
            }
        }
    }

    private static void specifyPaginationToHibernateQuery(final NativeQuery<?> query, final Integer pageNumber, final Integer pageCapacity) {
        if (pageNumber != null && pageCapacity != null) {
            query.setFirstResult(pageNumber * pageCapacity)
                 .setFetchSize(pageCapacity)
                 .setMaxResults(pageCapacity);
        }
    }
}

/* ********* Note 1: NVARCHAR column with SQL Server.

The following, in a top-level query, will cause a runtime error from the JDBC driver for SQL Server.

yield().val("hello").as("text.coreText")

The error says "The conversion from varchar to NCHAR is unsupported."

val("hello") is bound as a parameter with type VARCHAR because it contains only ASCII.
Yield text.coreText is specified as a resulting field with type NVARCHAR to the Hibernate query because RichText.coreText
has persistent type "nstring" which Hibernate maps to StringNVarcharType.

Upon closer inspection, the driver has rules for type conversion, which do not include varchar-to-nvarchar.
At the same time, there is enum JDBCType in the driver, and the following can be observed:

JDBCType.VARCHAR.convertsTo(JDBCType.NVARCHAR) == true // the case that produced the said error
JDBCType.NVARCHAR.convertsTo(JDBCType.VARCHAR) == false

The conversion rules are defined by enum GetterConversion, which operates on *categories* (instances of enum SSType are
categorised into instances of enum Category).

SSType.VARCHAR = {category: CHARACTER, jdbcType: VARCHAR}
SSType.NVARCHAR = {category: NCHARACTER, jdbcType: NVARCHAR}

To the contrary of the above JDBCType conversions, GetterConversion contains the opposite conversion:
NCHARACTER -> CHARACTER (i.e., NVARCHAR -> VARCHAR), but not vice versa.

It is not clear why in the context of "getter conversion" the rules are reversed.

Also, according to SQL Server conversion rules [1], VARCHAR is implicitly convertible to NVARCHAR and vice versa.

Our solution is to avoid specifying NVARCHAR as a result column type, and leave it up to the SQL Server to perform
auto-detection of the type.
It has been observed that it correcltly identifies strings that contain unicode as NVARCHAR, and strings that don't - as VARCHAR.
This prevents the observed error while preserving data integrity since both VARCHAR (ASCII strings) and NVARCHAR (unicode strings)
will be retrieved as instances of Java String.

For example:

1. VARCHAR is auto-detected for ascii strings.

   yield().val("hello").as("text.coreText")

   The parameter corresponding to the yielded value is bound with type VARCHAR.
   The column corresponding to `text.coreText` is specified with result type VARCHAR, and the retrieved string contains "hello" as expected.

2. NVARCHAR is auto-detected for unicode strings.

   yield().val("привіт").as("text.coreText")

   The parameter corresponding to the yielded value is bound with type NVARCHAR.
   The column corresponding to `text.coreText` is specified with result type NVARCHAR, and the retrieved string contains "привіт" as expected.

Effectively, the type of the parameter correspoding to the yielded value and the type of the column corresponding to the yield will always match.

[1] https://learn.microsoft.com/en-us/sql/t-sql/data-types/media/cast-and-convert-transact-sql/data-type-conversions.png?view=sql-server-ver16
 */
