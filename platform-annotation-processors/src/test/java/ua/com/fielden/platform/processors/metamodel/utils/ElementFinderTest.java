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

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.junit.Test;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import ua.com.fielden.platform.processors.test_utils.Compilation;
import ua.com.fielden.platform.processors.test_utils.InMemoryJavaFileManager;
import ua.com.fielden.platform.utils.CollectionUtil;

/**
 * A test case for utility functions in {@link ElementFinder}.
 *
 * @author TG Team
 *
 */
public class ElementFinderTest {

    // TODO MetaModelLifecycleTest uses a similar value, so it should be generalized
    private static final TypeSpec PLACEHOLDER = TypeSpec.classBuilder(nextTypeName("Placeholder")).build();

    @Test
    public void test_equals() {
        processAndEvaluate(finder -> {
            assertTrue(finder.equals(finder.getTypeElement(String.class), String.class));
            assertTrue(finder.equals(finder.getTypeElement(java.util.Date.class), java.util.Date.class));
            // java.sql.Date != java.util.Date
            assertFalse(finder.equals(finder.getTypeElement(java.sql.Date.class), java.util.Date.class));
            // java.util.Date != java.sql.Date
            assertFalse(finder.equals(finder.getTypeElement(java.util.Date.class), java.sql.Date.class));
        });
    }

    @Test
    public void findSuperclass_returns_the_immediate_superclass_type_element() {
        final TypeSpec iface = TypeSpec.interfaceBuilder(nextTypeName("IFace")).build();
        final TypeSpec sup = TypeSpec.classBuilder(nextTypeName("Sup")).build();
        // class Sub extends Sup implements Iface
        final TypeSpec sub = TypeSpec.classBuilder(nextTypeName("Sub"))
                .superclass(ClassName.get("", sup.name))
                .addSuperinterface(ClassName.get("", iface.name))
                .build();
        
        processAndEvaluate(List.of(iface, sup, sub), finder -> {
            final TypeElement superclassOfSub = finder.findSuperclass(finder.elements.getTypeElement(sub.name));
            assertEquals(finder.elements.getTypeElement(sup.name), superclassOfSub);
            assertEquals(finder.getTypeElement(Object.class), finder.findSuperclass(superclassOfSub));
        });
    }

    @Test 
    public void findSuperclass_returns_null_for_Object() {
        processAndEvaluate(finder -> assertNull(finder.findSuperclass(finder.getTypeElement(Object.class))));
    }

    @Test 
    public void findSuperclass_returns_null_for_an_interface() {
        final TypeSpec isup = TypeSpec.interfaceBuilder(nextTypeName("ISup")).build();
        final TypeSpec isub = TypeSpec.interfaceBuilder(nextTypeName("ISub"))
                .addSuperinterface(ClassName.get("", isup.name))
                .build();

        processAndEvaluate(List.of(isup, isub), finder -> {
            assertNull(finder.findSuperclass(finder.elements.getTypeElement(isup.name)));
            assertNull(finder.findSuperclass(finder.elements.getTypeElement(isub.name)));
        });
    }

    @Test
    public void findSuperclasses_returns_an_ordered_hierarchy_of_superclasses() {
        final TypeSpec iface = TypeSpec.interfaceBuilder(nextTypeName("IFace")).build();
        // class Sup implements IFace
        final TypeSpec sup = TypeSpec.classBuilder(nextTypeName("Sup")).build();
        // class Sub extends Sup implements IFace
        final TypeSpec sub = TypeSpec.classBuilder(nextTypeName("Sub"))
                .superclass(ClassName.get("", sup.name))
                .addSuperinterface(ClassName.get("", iface.name))
                .build();

        processAndEvaluate(List.of(iface, sup, sub), finder -> {
            // superclasses of Sub = [Sup, Object]
            assertEquals(Stream.of(sup.name, Object.class.getCanonicalName()).map(s -> finder.elements.getTypeElement(s)).toList(), 
                    finder.findSuperclasses(finder.elements.getTypeElement(sub.name)));
        });
    }

    // this test requires us to use member classes, because ElementFinder.findSuperclasses expects a Class parameter 
    @Test
    public void findSuperclasses_with_root_type_returns_an_ordered_hierarchy_of_superclasses_up_to_root_type_included() {
        processAndEvaluate(finder -> {
            // superclasses of Sub (root = Object) = [Sup, Object]
            assertEquals(Stream.of(FindSuperclasses_Sup.class, Object.class).map(c -> finder.getTypeElement(c)).toList(),
                    finder.findSuperclasses(finder.getTypeElement(FindSuperclasses_Sub.class), Object.class));
            // superclasses of Sub (root = Sup) = [Sup]
            assertEquals(List.of(finder.getTypeElement(FindSuperclasses_Sup.class)), 
                    finder.findSuperclasses(finder.getTypeElement(FindSuperclasses_Sub.class), FindSuperclasses_Sup.class));
            // superclasses of Sub (root = Sub) = []
            assertEquals(List.of(), finder.findSuperclasses(finder.getTypeElement(FindSuperclasses_Sub.class), FindSuperclasses_Sub.class));
            // unrelated hierarchies result into an empty list
            assertEquals(List.of(), finder.findSuperclasses(finder.getTypeElement(FindSuperclasses_Sub.class), String.class));
        });
    }
    // where
    private static interface FindSuperclasses_IFace {}
    private static class FindSuperclasses_Sup implements FindSuperclasses_IFace {}
    private static class FindSuperclasses_Sub extends FindSuperclasses_Sup {}

    @Test
    public void findDeclaredFields_returns_declared_fields_of_a_type_that_satisfy_predicate() {
        final FieldSpec static_final_String_STRING = FieldSpec.builder(String.class, "STRING", Modifier.FINAL, Modifier.STATIC).build();
        final FieldSpec String_str = FieldSpec.builder(String.class, "str").build();
        final FieldSpec final_int_i = FieldSpec.builder(int.class, "i", Modifier.FINAL).build();
        final TypeSpec example = TypeSpec.classBuilder(nextTypeName("Example"))
                .addFields(List.of(static_final_String_STRING, String_str, final_int_i))
                .build();

        processAndEvaluate(List.of(example), finder -> {
            // without any predicate all declared fields are returned
            assertEqualContents(example.fieldSpecs,
                    finder.findDeclaredFields(finder.elements.getTypeElement(example.name)).stream().map(f -> toFieldSpec(f)).toList());
            // with a predicate
            final Predicate<VariableElement> isFinal = (f -> f.getModifiers().contains(Modifier.FINAL));
            assertEqualContents(List.of(static_final_String_STRING, final_int_i),
                    finder.findDeclaredFields(finder.elements.getTypeElement(example.name), isFinal).stream().map(f -> toFieldSpec(f)).toList());
        });
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
                    Stream.of(sup1, sup2).flatMap(ts -> ts.fieldSpecs.stream()).toList(),
                    finder.findInheritedFields(finder.elements.getTypeElement(sub.name)).stream().map(f -> toFieldSpec(f)).toList());
        });
    }

    // ==================== HELPER METHODS ====================
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

    /**
     * Similar to {@link #processAndEvaluate(Collection, Consumer)}, but requires no input sources.
     */
    private void processAndEvaluate(final Consumer<ElementFinder> consumer) {
        processAndEvaluate(List.of(PLACEHOLDER), consumer);
    }

    private static void assertEqualContents(final Collection<?> c1, final Collection<?> c2) {
        if (isEqualContents(c1, c2)) {}
        else {
            fail("expected:<%s> but was:<%s>".formatted(CollectionUtil.toString(c1, ", "), CollectionUtil.toString(c2, ", ")));
        }
    }

}