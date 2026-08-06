package ua.com.fielden.platform.test.runners;

import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.test.WithDbVersion;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/// Verifies that the TG test runner correctly decides whether a test method should be ignored based on the [WithDbVersion] annotation.
///
/// The test constructs the runner directly against fixture classes and invokes the ignore predicate without running the actual tests.
/// Assertions are made relative to the current [DbVersion], so this suite is meaningful in any database environment.
///
public class WithDbVersionTest extends AbstractDaoTestCase {

    @Test
    public void class_level_annotation_applies_to_inherited_test_methods() throws Exception {
        final var runner = new H2OrPostgreSqlOrSqlServerContextSelector(MssqlOnly.class);
        final boolean expectIgnored = currentDbVersion() != DbVersion.MSSQL;

        assertEquals("A test method declared by the annotated class should be ignored iff the current DB version does not match.",
                     expectIgnored, runner.isIgnored(findMethod(runner, "declared_test")));
        assertEquals("A test method inherited from an unannotated base class should be treated the same as a declared one.",
                     expectIgnored, runner.isIgnored(findMethod(runner, "inherited_test")));
    }

    @Test
    public void class_level_annotation_is_inherited_by_subclasses() throws Exception {
        final var runner = new H2OrPostgreSqlOrSqlServerContextSelector(DerivedFromMssqlOnlyBase.class);
        final boolean expectIgnored = currentDbVersion() != DbVersion.MSSQL;

        assertEquals("A class-level annotation on a base class should apply to test methods declared by a subclass.",
                     expectIgnored, runner.isIgnored(findMethod(runner, "declared_test")));
        assertEquals("A class-level annotation on a base class should apply to test methods declared by that base class.",
                     expectIgnored, runner.isIgnored(findMethod(runner, "inherited_test")));
    }

    @Test
    public void method_level_annotation_takes_precedence_over_a_class_level_one() throws Exception {
        final var runner = new H2OrPostgreSqlOrSqlServerContextSelector(MssqlOnly.class);

        assertFalse("A method-level annotation that matches any DB version should not be ignored, regardless of the class-level annotation.",
                    runner.isIgnored(findMethod(runner, "any_db_test")));
    }

    // ---- Fixture classes used as input to the runner under test ----

    public static class UnannotatedBase extends AbstractDaoTestCase {
        @Test
        public void inherited_test() {}
    }

    @WithDbVersion(DbVersion.MSSQL)
    public static class MssqlOnly extends UnannotatedBase {
        @Test
        public void declared_test() {}

        @Test
        @WithDbVersion({DbVersion.H2, DbVersion.POSTGRESQL, DbVersion.MSSQL, DbVersion.ORACLE, DbVersion.MYSQL})
        public void any_db_test() {}
    }

    @WithDbVersion(DbVersion.MSSQL)
    public static class MssqlOnlyBase extends AbstractDaoTestCase {
        @Test
        public void inherited_test() {}
    }

    public static class DerivedFromMssqlOnlyBase extends MssqlOnlyBase {
        @Test
        public void declared_test() {}
    }

    // ---- Helpers ----

    private DbVersion currentDbVersion() {
        return getInstance(IDbVersionProvider.class).dbVersion();
    }

    private static FrameworkMethod findMethod(final H2OrPostgreSqlOrSqlServerContextSelector runner, final String name) {
        return runner.getTestClass().getAnnotatedMethods(Test.class).stream()
                .filter(m -> m.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Fixture is missing test method [%s].".formatted(name)));
    }

}
