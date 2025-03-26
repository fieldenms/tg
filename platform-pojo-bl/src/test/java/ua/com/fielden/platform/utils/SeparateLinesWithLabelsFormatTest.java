package ua.com.fielden.platform.utils;

import org.junit.Test;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.ToString.IFormat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.ToString.SeparateLinesWithLabelsFormat.MAX_UNLABELED_VALUE_LENGTH;
import static ua.com.fielden.platform.utils.ToString.separateLines;

public class SeparateLinesWithLabelsFormatTest {

    private static final Symbol LIST = new Symbol("LIST");
    private static final Symbol CONS = new Symbol("CONS");

    @Test
    public void formattable_object_is_fully_represented_only_once_when_shared_by_a_collection() {
        final var data = new Data().setCollection(List.of(LIST, List.of(), CONS, LIST, List.of(CONS, List.of(List.of(LIST)))));

        assertThat(data.toString(separateLines()))
                .containsOnlyOnce(CONS.name())
                .containsOnlyOnce(LIST.name());
    }

    @Test
    public void formattable_object_is_fully_represented_only_once_when_shared_as_a_map_entry_value() {
        final var data = new Data().setMap(Map.of("a", LIST, "b", LIST, "c", CONS, "d", List.of(CONS), "e", Map.of(1, List.of(CONS, LIST))));

        assertThat(data.toString(separateLines()))
                .containsOnlyOnce(CONS.name())
                .containsOnlyOnce(LIST.name());
    }

    @Test
    public void formattable_object_is_fully_represented_only_once_when_shared_between_fields() {
        {
            final var data = new Data().setObject1(CONS).setObject2(CONS);
            assertThat(data.toString(separateLines()))
                    .containsOnlyOnce(CONS.name());
        }

        {
            final var data = new Data()
                    .setObject1(new Data().setObject1(CONS).setObject2(new Data().setObject2(LIST)))
                    .setObject2(List.of(LIST, CONS));
            assertThat(data.toString(separateLines()))
                    .containsOnlyOnce(CONS.name())
                    .containsOnlyOnce(LIST.name());
        }
    }

    @Test
    public void formattable_object_is_fully_represented_only_once_when_shared_by_elements_of_a_pair() {
        {
            final var data = new Data().setPair(t2(CONS, CONS));
            assertThat(data.toString(separateLines()))
                    .containsOnlyOnce(CONS.name());
        }

        {
            final var data = new Data()
                    .setPair(t2(new Data().setObject1(LIST),
                                List.of(CONS, Map.of(1, new Data().setPair(t2(List.of(CONS, LIST),
                                                                              new Data().setObject2(CONS)))))));
            assertThat(data.toString(separateLines()))
                    .containsOnlyOnce(CONS.name())
                    .containsOnlyOnce(LIST.name());
        }
    }

    @Test
    public void non_formattable_object_is_fully_represented_only_once_if_its_string_representation_is_longer_than_limit() {
        final var longSymbol = new PlainSymbol("a".repeat(MAX_UNLABELED_VALUE_LENGTH + 1));
        final var shortSymbol = new PlainSymbol("Short");

        final var data = new Data()
                .setObject1(longSymbol)
                .setObject2(shortSymbol)
                .setCollection(List.of(longSymbol, shortSymbol))
                .setPair(t2(longSymbol, shortSymbol))
                .setMap(Map.of(1, shortSymbol, 2, longSymbol));
        assertThat(data.toString(separateLines()))
                .containsOnlyOnce(longSymbol.name())
                .satisfies(s -> assertEquals(4, countMatches(s, shortSymbol.name())));
    }

    @Test
    public void non_formattable_directly_circular_object_is_fully_represented_only_once() {
        final var loop = new PlainDirectlyCircular("Loop");
        assertThat(loop.toString()).containsOnlyOnce(loop.id);
    }

    @Test
    public void non_formattable_indirectly_circular_object_cannot_be_represented() {
        final var loop = new PlainIndirectlyCircular1();
        assertThatThrownBy(loop::toString)
                .isInstanceOf(StackOverflowError.class);
    }

    @Test
    public void formattable_circular_object_is_fully_represented_only_once_when_it_is_the_root_of_formatting() {
        final var loop = new Circular("Loop");
        assertThat(loop.toString(separateLines()))
                .containsOnlyOnce(loop.id);
    }

    @Test
    public void formattable_circular_object_is_fully_represented_only_once_when_it_is_not_the_root_of_formatting() {
        final var loop = new Circular("Loop");
        final var data = new Data().setObject1(loop);
        assertThat(data.toString(separateLines()))
                .containsOnlyOnce(loop.id);
    }

    private static final class Data implements ToString.IFormattable {

        private Collection<?> collection;
        private Map<?, ?> map;
        private T2<?, ?> pair;
        private Object object1;
        private Object object2;

        private Data() {}

        @Override
        public String toString(final IFormat format) {
            return format.toString(this)
                    .addIfNotNull("list", collection)
                    .addIfNotNull("object1", object1)
                    .addIfNotNull("object2", object2)
                    .addIfNotNull("map", map)
                    .addIfNotNull("pair", pair)
                    .$();
        }

        public Collection<?> collection() {
            return collection;
        }

        public Object object1() {
            return object1;
        }

        public Object getObject2() {
            return object2;
        }

        public Map<?, ?> map() {
            return map;
        }

        public T2<?, ?> pair() {
            return pair;
        }

        public Data setObject1(final Object object1) {
            this.object1 = object1;
            return this;
        }

        public Data setCollection(final Collection<?> collection) {
            this.collection = collection;
            return this;
        }

        public Data setMap(final Map<?, ?> map) {
            this.map = map;
            return this;
        }

        public Data setPair(final T2<?, ?> pair) {
            this.pair = pair;
            return this;
        }

        public Data setObject2(final Object object2) {
            this.object2 = object2;
            return this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(collection, object1, map, pair);
        }

    }

    private record Symbol (String name) implements ToString.IFormattable {

        @Override
        public String toString(final IFormat format) {
            return format.toString(this)
                    .add("name", name)
                    .$();
        }

    }

    private record PlainSymbol (String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private static final class Circular implements ToString.IFormattable {
        final String id;
        Circular self = this;

        private Circular(final String id) {
            this.id = id;
        }

        @Override
        public String toString(final IFormat format) {
            return format.toString(this)
                    .add("id", id)
                    .add("self", self)
                    .$();
        }

    }

    private static final class PlainDirectlyCircular {
        final String id;
        PlainDirectlyCircular self = this;

        private PlainDirectlyCircular(final String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return separateLines().toString(this)
                    .add("id", id)
                    .add("self", self)
                    .$();
        }

    }

    /**
     * This class demonstrates why the circularity of non-formattable objects cannot be detected.
     * Here method {@link #toString()} always creates a new format, as opposed to {@link ToString.IFormattable#toString(IFormat)}
     * that uses the supplied format.
     * Therefore, when {@link #toString()} is called on {@link #self}, the enclosing format that called it cannot be used,
     * and the process begins anew.
     */
    private static final class PlainIndirectlyCircular1 {

        PlainIndirectlyCircular2 that = new PlainIndirectlyCircular2(this);

        @Override
        public String toString() {
            return separateLines().toString(this)
                    .add("that", that)
                    .$();
        }

    }

    private static final class PlainIndirectlyCircular2 {

        PlainIndirectlyCircular1 that;

        public PlainIndirectlyCircular2(final PlainIndirectlyCircular1 that) {
            this.that = that;
        }

        @Override
        public String toString() {
            return separateLines().toString(this)
                    .add("that", that)
                    .$();
        }

    }


}
