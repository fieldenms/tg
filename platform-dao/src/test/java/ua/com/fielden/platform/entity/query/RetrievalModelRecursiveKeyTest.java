package ua.com.fielden.platform.entity.query;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.*;

/// This test covers the construction of retrieval models for entity types with recursive key structures.
///
public class RetrievalModelRecursiveKeyTest extends AbstractDaoTestCase implements IRetrievalModelTestUtils {

    @Test
    public void retrieval_model_that_includes_key_for_recursive_key_structure_is_truncated() {
        assertThat(List.of(ALL, ALL_INCL_CALC, KEY_AND_DESC))
                .allSatisfy(cat -> assertRetrievalModel(A.class, cat)
                        .subModel("b", a -> a.contains("a"))
                        .subModel("b.a", a -> a.contains("b"))
                        .subModel("b.a.b", a -> a.equalsModel(ID_ONLY)));

        assertThat(List.of(DEFAULT))
                .allSatisfy(cat -> assertRetrievalModel(A.class, cat)
                        .subModel("b", a -> a.contains("a"))
                        .subModel("b.a", a -> a.equalsModel(ID_ONLY)));
    }

    @Test
    public void retrieval_model_that_does_not_include_key_can_be_constructed_for_recursive_key_structure() {
        assertThat(List.of(ID_ONLY, ID_AND_VERSION, NONE))
                .allSatisfy(cat -> assertRetrievalModel(A.class, cat).notContains(KEY));
    }

    @Override
    protected void populateDomain() {}

    @KeyType(DynamicEntityKey.class)
    @MapEntityTo
    private static class A extends AbstractEntity<DynamicEntityKey> {

        @IsProperty
        @CompositeKeyMember(1)
        @MapTo
        private String name;

        @IsProperty
        @CompositeKeyMember(2)
        @MapTo
        @Optional
        private B b;

        @Observable
        public A setB(final B b) {
            this.b = b;
            return this;
        }

        public B getB() {
            return b;
        }

        public String getName() {
            return name;
        }

        @Observable
        public A setName(final String name) {
            this.name = name;
            return this;
        }
    }

    @KeyType(DynamicEntityKey.class)
    @MapEntityTo
    private static class B extends AbstractEntity<DynamicEntityKey> {

        @IsProperty
        @CompositeKeyMember(1)
        @MapTo
        private String number;

        @IsProperty
        @CompositeKeyMember(2)
        @MapTo
        @Optional
        private A a;

        @Observable
        public B setA(final A a) {
            this.a = a;
            return this;
        }

        public A getA() {
            return a;
        }

        public String getNumber() {
            return number;
        }

        @Observable
        public B setNumber(final String number) {
            this.number = number;
            return this;
        }
    }

}
