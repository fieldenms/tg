package ua.com.fielden.platform.processors.utils;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.test_utils.ProcessingRule;

import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class TypeSetTest {

    @ClassRule
    public static ProcessingRule rule = new ProcessingRule();
    private static Elements elements;
    private static Types types;
    private static ElementFinder elementFinder;

    @BeforeClass
    public static void setup() {
        elements = rule.getElements();
        types = rule.getTypes();
        elementFinder = new ElementFinder(rule.getProcessingEnvironment());
    }

    @Test
    public void size_reports_correct_size() {
        assertEquals(2, TypeSet.ofClasses(int.class, List.class).size());
        assertEquals(1, TypeSet.ofClasses(List.class).size());
        assertEquals(0, TypeSet.ofClasses().size());
    }

    @Test
    public void isEmpty_returns_true_only_when_TypeSet_is_empty() {
        assertTrue(TypeSet.ofClasses().isEmpty());
        assertFalse(TypeSet.ofClasses(int.class).isEmpty());
        assertFalse(TypeSet.ofClasses(Integer.class, Double.class).isEmpty());
    }

    @Test
    public void elements_with_the_same_canonical_names_are_deduplicated() {
        final var set = TypeSet.build(b -> b
                .add(int.class).add(int.class)
                .add(char.class).add(types.getPrimitiveType(TypeKind.CHAR))
                .add(String.class).add(elementFinder.getTypeElement(String.class).asType())
                .add(List.class).add(types.getDeclaredType(elementFinder.getTypeElement(List.class),
                                                           elementFinder.getTypeElement(String.class).asType()))
                .add(Double.class).add(Double.class));
        assertEquals(5, set.size());
        assertTrue(set.contains(int.class));
        assertTrue(set.contains(types.getPrimitiveType(TypeKind.INT)));
        assertTrue(set.contains(elementFinder.getTypeElement(Double.class).asType()));
    }

    @Test
    public void TypeSet_contains_works_correctly_for_TypeMirror_arguments() {
        final var set = TypeSet.ofClasses(int.class, Integer.class, List.class, String.class);
        assertTrue(set.contains(types.getPrimitiveType(TypeKind.INT)));
        assertFalse(set.contains(types.getPrimitiveType(TypeKind.CHAR)));
        // List<String>
        assertTrue(set.contains(types.getDeclaredType(elementFinder.getTypeElement(List.class),
                                                      elementFinder.getTypeElement(String.class).asType())));
        assertTrue(set.contains(elementFinder.getTypeElement(Integer.class).asType()));
    }

    @Test
    public void TypeSet_contains_works_correctly_for_Class_arguments() {
        final var set = TypeSet.ofClasses(int.class, Integer.class, List.class, String.class);
        assertTrue(set.contains(int.class));
        assertTrue(set.contains(Integer.class));
        assertTrue(set.contains(List.class));
        assertTrue(set.contains(String.class));
        assertFalse(set.contains(double.class));
        assertFalse(set.contains(Set.class));
    }

}
