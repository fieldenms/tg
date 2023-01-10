package ua.com.fielden.platform.processors.metamodel.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.utils.CollectionUtil.isEqualContents;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.testing.compile.CompilationRule;

import ua.com.fielden.platform.utils.CollectionUtil;

/**
 * A test case for utility functions in {@link ElementFinder}.
 *
 * @author TG Team
 *
 */
public class ElementFinderTest {

    public @Rule CompilationRule rule = new CompilationRule();
    private Elements elements;
    private Types types;
    private ElementFinder finder;

    @Before
    public void setup() {
      elements = rule.getElements();
      types = rule.getTypes();
      finder = new ElementFinder(elements, types);
    }

    @Test
    public void test_equals() {
        assertTrue(finder.equals(finder.getTypeElement(String.class), String.class));
        assertTrue(finder.equals(finder.getTypeElement(java.util.Date.class), java.util.Date.class));
        // java.sql.Date != java.util.Date
        assertFalse(finder.equals(finder.getTypeElement(java.sql.Date.class), java.util.Date.class));
        // java.util.Date != java.sql.Date
        assertFalse(finder.equals(finder.getTypeElement(java.util.Date.class), java.sql.Date.class));
    }

    @Test
    public void findSuperclass_returns_the_immediate_superclass_type_element() {
        final TypeElement superclassOfSub = finder.findSuperclass(finder.getTypeElement(Sub.class));
        assertEquals(finder.getTypeElement(Super.class), superclassOfSub);
        assertEquals(finder.getTypeElement(Object.class), finder.findSuperclass(superclassOfSub));
    }

    @Test 
    public void findSuperclass_returns_null_for_Object() {
        assertNull(finder.findSuperclass(finder.getTypeElement(Object.class)));
    }

    @Test 
    public void findSuperclass_returns_null_for_an_interface() {
        assertNull(finder.findSuperclass(finder.getTypeElement(ISub.class)));
        assertNull(finder.findSuperclass(finder.getTypeElement(ISuper.class)));
    }

    @Test
    public void findSuperclasses_returns_an_ordered_hierarchy_of_superclasses() {
        assertEquals(Stream.of(Super.class, Object.class).map(c -> finder.getTypeElement(c)).toList(), 
                finder.findSuperclasses(finder.getTypeElement(Sub.class)));
    }

    @Test
    public void findSuperclasses_with_root_type_returns_an_ordered_hierarchy_of_superclasses_up_to_root_type_included() {
        assertEquals(Stream.of(Super.class, Object.class).map(c -> finder.getTypeElement(c)).toList(),
                finder.findSuperclasses(finder.getTypeElement(Sub.class), Object.class));
        assertEquals(List.of(finder.getTypeElement(Super.class)), finder.findSuperclasses(finder.getTypeElement(Sub.class), Super.class));
        assertEquals(List.of(), finder.findSuperclasses(finder.getTypeElement(Sub.class), Sub.class));
        // unrelated hierarchies result into an empty list
        assertEquals(List.of(), finder.findSuperclasses(finder.getTypeElement(Sub.class), String.class));
    }

    @Test
    public void findDeclaredFields_returns_declared_fields_of_a_type_that_satisfy_predicate() {
        // without any predicate all declared fields are returned
        assertEqualContents(List.of("STRING", "str", "i"),
                streamSimpleNames(finder.findDeclaredFields(finder.getTypeElement(Sub.class))).toList());
        // with predicate
        final Predicate<VariableElement> isFinal = (f -> f.getModifiers().contains(Modifier.FINAL));
        assertEqualContents(List.of("STRING", "i"),
                streamSimpleNames(finder.findDeclaredFields(finder.getTypeElement(Sub.class), isFinal)).toList());
    }

    private static interface ISuper {}
    private static interface ISub extends ISuper {}

    private static class Super implements ISuper {
        int n;
        private double d;
    }

    private static class Sub extends Super implements ISub {
        final static String STRING = "hello";
        String str;
        final int i = 10;
    }

    private static <T extends Element> Stream<String> streamSimpleNames(final Collection<T> c) {
        return c.stream().map(e -> e.getSimpleName().toString());
    }

    private static void assertEqualContents(final Collection<?> c1, final Collection<?> c2) {
        if (isEqualContents(c1, c2)) {}
        else {
            fail("expected:<%s> but was:<%s>".formatted(CollectionUtil.toString(c1, ", "), CollectionUtil.toString(c2, ", ")));
        }
    }

}