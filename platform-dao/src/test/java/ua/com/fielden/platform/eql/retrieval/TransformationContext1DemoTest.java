package ua.com.fielden.platform.eql.retrieval;


import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;

public class TransformationContext1DemoTest extends AbstractEqlShortcutTest {
    @Rule
    public TestName name = new TestName();

    @Before
    public void setUp() {
        System.out.println("\nEXECUTING TEST [%s]:\n".formatted(name.getMethodName()));
        TransformationContextFromStage1To2.SHOW_INTERNALS = true;
    }

    @After
    public void tearDown() {
        TransformationContextFromStage1To2.SHOW_INTERNALS = false;
    }

    @Test
    public void demo_1() {
        final var mr = transformToModelResult(
                select(VEHICLE).
                where().
                prop("station").isNotNull().
                yield().countAll().as("kount").modelAsAggregate()
                );
        System.out.println(mr.sql());
    }

    @Test
    public void demo_2() {
        final var mr = transformToModelResult(
                select(VEHICLE).
                leftJoin(ORG5).as("s").
                on().
                prop("station").eq().prop("s").
                where().
                prop("name").eq().val(1000).
                yield().countAll().as("kount").modelAsAggregate()
                );
        System.out.println(mr.sql());
    }

    @Test
    public void demo_3() {
        final var mr = transformToModelResult(
                select(VEHICLE).
                leftJoin(ORG5).as("s").
                on().
                prop("station").eq().prop("s").
                leftJoin(MODEL).as("m").
                on().
                prop("model").eq().prop("m").
                where().
                prop("name").eq().val(5000).
                and().
                prop("m.make").eq().val(17).
                yield().countAll().as("kount").modelAsAggregate()
                );
        System.out.println(mr.sql());
    }

    @Test
    public void demo_4() {
        final var mr = transformToModelResult(
                select(VEHICLE).
                where().
                exists(
                        select(ORG5).
                        where().
                        prop("id").eq().prop("station").
                        and().
                        prop("name").eq().val(5000).
                        model()
                        ).
                yield().countAll().as("kount").modelAsAggregate()
                );
        System.out.println(mr.sql());
    }


    @Test
    public void demo_5() {
        final var mr = transformToModelResult(
                select(VEHICLE).
                where().
                exists(
                        select(ORG5).
                        where().
                        prop("id").eq().prop("station").
                        and().
                        exists(
                                select(ORG4).
                                where().
                                prop("id").eq().extProp("parent").
                                and().
                                prop("name").eq().val(4000).
                                model()
                                ).
                        model()
                        ).
                yield().countAll().as("kount").modelAsAggregate()
                );
        System.out.println(mr.sql());
    }

    @Test
    public void demo_6() {
        final var mr = transformToModelResult(
                select(VEHICLE).
                where().
                exists(
                        select(ORG5).
                        where().
                        prop("id").eq().prop("station").
                        and().
                        exists(
                                select(ORG4).
                                where().
                                prop("id").eq().extProp("parent").
                                and().
                                exists(
                                        select(ORG3).
                                        where().
                                        prop("id").eq().extProp("parent").
                                        and().
                                        prop("name").eq().val(3000).
                                        model()
                                        ).
                                model()
                                ).
                        model()
                        ).
                yield().countAll().as("kount").modelAsAggregate()
                );
        System.out.println(mr.sql());
    }

    @Test
    public void demo_7() {
        final var mr = transformToModelResult(
                select(VEHICLE).
                where().
                exists(
                        select(ORG5).as("o5").
                        join(ORG4).as("o4").
                        on().
                        prop("o5.parent").eq().prop("o4").
                        where().
                        prop("o5").eq().prop("station").
                        and().
                        exists(
                                select(ORG3).
                                where().
                                prop("id").eq().prop("o4.parent").
                                and().
                                prop("name").eq().val(3000).
                                model()
                                ).
                        model()
                        ).
                yield().countAll().as("kount").modelAsAggregate()
                );
        System.out.println(mr.sql());
    }
}