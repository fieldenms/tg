package ua.com.fielden.platform.processors.metamodel.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.processors.test_utils.Compilation.OPTION_PROC_ONLY;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService.nextTypeName;
import static ua.com.fielden.platform.utils.CollectionUtil.isEqualContents;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.testing.compile.CompilationRule;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import ua.com.fielden.platform.processors.test_utils.Compilation;
import ua.com.fielden.platform.processors.test_utils.InMemoryJavaFileManager;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;
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

    @Test
    public void findInheritedFields_returns_all_inherited_fields() {
        final TypeSpec sup2 = TypeSpec.classBuilder(nextTypeName("Sup2"))
                // protected String s
                .addField(String.class, "s", Modifier.PROTECTED)
                // private Integer i
                .addField(Integer.class, "i", Modifier.PRIVATE)
                // double d
                .addField(double.class, "d")
                .build();
        final TypeSpec sup1 = TypeSpec.classBuilder(nextTypeName("Sup1"))
                .superclass(ClassName.get("", sup2.name))
                // public String s
                .addField(String.class, "s", Modifier.PUBLIC)
                // private Integer i
                .addField(Integer.class, "i", Modifier.PRIVATE)
                // boolean b
                .addField(boolean.class, "b")
                .build();
        final TypeSpec sub = TypeSpec.classBuilder(nextTypeName("Sub"))
                .superclass(ClassName.get("", sup1.name))
                // these should not be included
                .addField(String.class, "s")
                .addField(String.class, "name")
                .build();

        processAndEvaluate(List.of(sub, sup1, sup2), finder -> {
            assertEqualContents(
                    Stream.concat(sup1.fieldSpecs.stream(), sup2.fieldSpecs.stream()).toList(),
                    finder.findInheritedFields(finder.elements.getTypeElement(sub.name)).stream().map(f -> toFieldSpec(f)).toList());
        });
    }

    /**
     * A convenient method to convert a {@link VariableElement} to a {@link FieldSpec} for further comparison.
     * Ignores annotations.
     */
    private FieldSpec toFieldSpec(final VariableElement el) {
        return FieldSpec.builder(TypeName.get(el.asType()), el.getSimpleName().toString(), el.getModifiers().toArray(Modifier[]::new))
                .build();
    }

    /**
     * Simulates annotation processing of sources represented by {@code typeSpecs} and provides an instance of {@link ElementFinder}
     * to {@code consumer}, returning after it's done.
     * <p>
     * {@code typeSpecs} are assumed to reside in an unnamed package.
     *
     * @throws RuntimeException if an exception was thrown during annotation processing
     */
    private void processAndEvaluate(final Collection<TypeSpec> typeSpecs, final Consumer<ElementFinder> consumer) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final InMemoryJavaFileManager fileManager = new InMemoryJavaFileManager(compiler.getStandardFileManager(null, null, null));
        final List<JavaFileObject> compilationTargets = typeSpecs.stream()
                .map(ts -> JavaFile.builder(/*packageName*/ "", ts).build().toJavaFileObject())
                .toList();

        try {
            new Compilation(compilationTargets)
                    .setCompiler(compiler)
                    .setFileManager(fileManager)
                    .setOptions(OPTION_PROC_ONLY)
                    .compileAndEvaluate(procEnv -> consumer.accept(new ElementFinder(procEnv.getElementUtils(), procEnv.getTypeUtils())));
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
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