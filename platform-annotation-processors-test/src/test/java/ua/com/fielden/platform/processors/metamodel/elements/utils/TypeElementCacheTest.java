package ua.com.fielden.platform.processors.metamodel.elements.utils;

import com.google.testing.compile.CompilationRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.Assert.*;

/**
 * A test case for {@link TypeElementCache}.
 *
 * @author TG Team
 */
public class TypeElementCacheTest {

    @Rule public final CompilationRule rule1 = new CompilationRule();
    @Rule public final CompilationRule rule2 = new CompilationRule();


    @After
    public void after() {
        // clear the cache after each @Test to achieve test isolation
        TypeElementCache.clear();
    }

    @Test
    public void getTypeElement_returns_the_expected_element() {
        final Consumer<Elements> assertor = (elements) -> {
            final TypeElement objectElt = TypeElementCache.getTypeElement(elements, Object.class.getCanonicalName());
            assertNotNull(objectElt);
            assertSame(objectElt, elements.getTypeElement(Object.class.getCanonicalName()));
        };

        assertor.accept(rule1.getElements());
        assertor.accept(rule2.getElements());
    }

    @Test
    public void elements_are_cached_upon_retrieval() {
        final Elements elements = rule1.getElements();

        final TypeElement objectElt = TypeElementCache.getTypeElement(elements, Object.class.getCanonicalName());
        assertNotNull(objectElt);

        final Map<String, TypeElement> cacheView = TypeElementCache.cacheViewFor(elements).orElseThrow();
        assertEquals("Element should have been cached.", Set.of(Object.class.getCanonicalName()), cacheView.keySet());
        final TypeElement cachedObjectElt = cacheView.get(Object.class.getCanonicalName());
        assertNotNull(cachedObjectElt);
        assertSame("Cached and returned elements should refer to the same instance.", objectElt, cachedObjectElt);
    }

    @Test
    public void null_values_are_not_cached() {
        final Elements elements = rule1.getElements();

        final TypeElement stubElt = TypeElementCache.getTypeElement(elements, "stub");
        assertNull(stubElt);

        final Map<String, TypeElement> cacheView = TypeElementCache.cacheViewFor(elements).orElseThrow();
        assertFalse(cacheView.containsKey("stub"));
    }

    @Test
    public void different_instances_of_Elements_have_independent_caches() {
        final Elements elements1 = rule1.getElements();
        TypeElementCache.getTypeElement(elements1, Object.class.getCanonicalName());
        TypeElementCache.getTypeElement(elements1, String.class.getCanonicalName());

        final Elements elements2 = rule2.getElements();
        TypeElementCache.getTypeElement(elements2, List.class.getCanonicalName());

        final Map<String, TypeElement> cacheView1 = TypeElementCache.cacheViewFor(elements1).orElseThrow();
        assertEquals(Set.of(Object.class.getCanonicalName(), String.class.getCanonicalName()), cacheView1.keySet());

        final Map<String, TypeElement> cacheView2 = TypeElementCache.cacheViewFor(elements2).orElseThrow();
        assertNotSame(cacheView1, cacheView2);
        assertEquals(Set.of(List.class.getCanonicalName()), cacheView2.keySet());
    }

    @Test
    public void elements_with_same_names_produced_by_different_instances_of_Elements_are_not_same_nor_equal() {
        final TypeElement objectElt1 = TypeElementCache.getTypeElement(rule1.getElements(), Object.class.getCanonicalName());
        assertNotNull(objectElt1);
        final TypeElement objectElt2 = TypeElementCache.getTypeElement(rule2.getElements(), Object.class.getCanonicalName());
        assertNotNull(objectElt2);

        assertNotSame(objectElt1, objectElt2);
        assertNotEquals(objectElt1, objectElt2);
    }

    @Test
    public void elements_with_same_names_produced_by_different_instances_of_Elements_cannot_be_meaningfully_compared() {
        final TypeElement objectElt1 = TypeElementCache.getTypeElement(rule1.getElements(), Object.class.getCanonicalName());
        assertNotNull(objectElt1);
        final TypeElement objectElt2 = TypeElementCache.getTypeElement(rule2.getElements(), Object.class.getCanonicalName());
        assertNotNull(objectElt2);

        // even though both type elements represent Object, their different origins prevent meaningful comparison
        assertFalse(rule1.getTypes().isSameType(objectElt1.asType(), objectElt2.asType()));
        assertFalse(rule2.getTypes().isSameType(objectElt1.asType(), objectElt2.asType()));
    }

}
