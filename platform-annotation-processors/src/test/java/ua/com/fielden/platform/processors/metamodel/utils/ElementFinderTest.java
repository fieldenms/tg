package ua.com.fielden.platform.processors.metamodel.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.processors.test_utils.Compilation.OPTION_PROC_ONLY;
import static ua.com.fielden.platform.utils.CollectionUtil.isEqualContents;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.junit.Test;

import com.squareup.javapoet.AnnotationSpec;
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
    private static final TypeSpec PLACEHOLDER = TypeSpec.classBuilder("Placeholder").build();

    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestAnnot {
        String value() default "default";
    }

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
        final TypeSpec iface = TypeSpec.interfaceBuilder("IFace").build();
        final TypeSpec sup = TypeSpec.classBuilder("Sup").build();
        // class Sub extends Sup implements Iface
        final TypeSpec sub = TypeSpec.classBuilder("Sub")
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
        final TypeSpec isup = TypeSpec.interfaceBuilder("ISup").build();
        final TypeSpec isub = TypeSpec.interfaceBuilder("ISub")
                .addSuperinterface(ClassName.get("", isup.name))
                .build();

        processAndEvaluate(List.of(isup, isub), finder -> {
            assertNull(finder.findSuperclass(finder.elements.getTypeElement(isup.name)));
            assertNull(finder.findSuperclass(finder.elements.getTypeElement(isub.name)));
        });
    }

    @Test
    public void findSuperclasses_returns_an_ordered_hierarchy_of_superclasses() {
        final TypeSpec iface = TypeSpec.interfaceBuilder("IFace").build();
        // class Sup implements IFace
        final TypeSpec sup = TypeSpec.classBuilder("Sup").build();
        // class Sub extends Sup implements IFace
        final TypeSpec sub = TypeSpec.classBuilder("Sub")
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
        final TypeSpec example = TypeSpec.classBuilder("Example")
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
        final TypeSpec sup2 = TypeSpec.classBuilder("Sup2")
                // static Object staticObj
                .addField(Object.class, "staticObj", Modifier.STATIC)
                // protected String s
                .addField(String.class, "s", Modifier.PROTECTED)
                // private Integer i
                .addField(Integer.class, "i", Modifier.PRIVATE)
                // double d
                .addField(double.class, "d")
                .build();
        final TypeSpec sup1 = TypeSpec.classBuilder("Sup1")
                .superclass(ClassName.get("", sup2.name))
                // public String s
                .addField(String.class, "s", Modifier.PUBLIC)
                // private Integer i
                .addField(Integer.class, "i", Modifier.PRIVATE)
                // boolean b
                .addField(boolean.class, "b")
                .build();
        final TypeSpec sub = TypeSpec.classBuilder("Sub")
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

    // TODO test findInheritedFields with root type after refactoring ElementFinder to accept a TypeElement as root type

    @Test
    public void findFields_returns_both_declared_and_inherited_fields() {
        final TypeSpec sup2 = TypeSpec.classBuilder("Sup2")
                // static Object staticObj
                .addField(Object.class, "staticObj", Modifier.STATIC)
                // protected String s
                .addField(String.class, "s", Modifier.PROTECTED)
                // private Integer i
                .addField(Integer.class, "i", Modifier.PRIVATE)
                // double d
                .addField(double.class, "d")
                .build();
        final TypeSpec sup1 = TypeSpec.classBuilder("Sup1")
                .superclass(ClassName.get("", sup2.name))
                // public String s
                .addField(String.class, "s", Modifier.PUBLIC)
                // private Integer i
                .addField(Integer.class, "i", Modifier.PRIVATE)
                // boolean b
                .addField(boolean.class, "b")
                .build();
        final TypeSpec sub = TypeSpec.classBuilder("Sub")
                .superclass(ClassName.get("", sup1.name))
                .addField(String.class, "s")
                .addField(String.class, "name")
                .build();

        processAndEvaluate(List.of(sub, sup1, sup2), finder -> {
            assertEqualContents(
                    Stream.of(sup2, sup1, sub).flatMap(ts -> ts.fieldSpecs.stream()).toList(),
                    finder.findFields(finder.elements.getTypeElement(sub.name)).stream().map(f -> toFieldSpec(f)).toList());
        });
    }

    @Test
    public void findField_returns_the_first_matching_field() {
        final TypeSpec sup = TypeSpec.classBuilder("Sup")
                .addField(int.class, "i")
                .addField(boolean.class, "b")
                .addField(String.class, "s", Modifier.STATIC)
                .build();
        final TypeSpec sub = TypeSpec.classBuilder("Sub")
                .superclass(ClassName.get("", sup.name))
                .addField(int.class, "i")
                .addField(String.class, "s")
                .build();

        processAndEvaluate(List.of(sup, sub), finder -> {
            final TypeElement subEl = finder.elements.getTypeElement(sub.name);
            final TypeElement supEl = finder.elements.getTypeElement(sup.name);

            // Sub.i
            assertEquals(subEl, finder.findField(subEl, "i").getEnclosingElement());
            // Sup.s
            assertEquals(supEl, finder.findField(subEl, "s", f -> finder.isStatic(f)).getEnclosingElement());
            // Sup.b
            assertEquals(supEl, finder.findField(subEl, "b").getEnclosingElement());
            // non-existent field
            assertNull(finder.findField(subEl, "noSuchField"));
            // non-existent field
            assertNull(finder.findField(subEl, "s", f -> f.getModifiers().contains(Modifier.FINAL)));
        });
    }

    @Test
    public void findFieldsAnnotatedWith_returns_all_fields_having_the_annotation() {
        final FieldSpec sProp = FieldSpec.builder(String.class, "s").addAnnotation(TestAnnot.class).build();
        final TypeSpec sup = TypeSpec.classBuilder("Sup")
                .addField(sProp)
                .addField(Integer.class, "i")
                .build();
        final FieldSpec supProp = FieldSpec.builder(ClassName.get("", sup.name), "sup").addAnnotation(TestAnnot.class).build();
        final TypeSpec sub = TypeSpec.classBuilder("Sub")
                .superclass(ClassName.get("", sup.name))
                .addField(Double.class, "d")
                .addField(supProp)
                .build();

        processAndEvaluate(List.of(sup, sub), finder -> {
            assertEqualContents(List.of(sProp, supProp),
                    finder.findFieldsAnnotatedWith(finder.elements.getTypeElement(sub.name), TestAnnot.class).stream()
                    .map(f -> toFieldSpec(f)).toList());
        });
    }

    @Test
    public void getFieldAnnotations_returns_a_list_of_directly_present_annotations() {
        // a single-use annotation type that will be present on an inherited field
        final TypeSpec oneTimeAnnotType = TypeSpec.annotationBuilder("OneTimeAnnot").build();

        final AnnotationSpec oneTimeAnnot = AnnotationSpec.builder(ClassName.get("", oneTimeAnnotType.name)).build();
        final TypeSpec sup = TypeSpec.classBuilder("Sup")
                .addField(FieldSpec.builder(String.class, "s").addAnnotation(oneTimeAnnot).build())
                .build();

        // @TestAnnot(value = "hello")
        final AnnotationSpec subFieldAnnot = AnnotationSpec.builder(TestAnnot.class).addMember("value", "$S", "hello").build();
        final TypeSpec sub = TypeSpec.classBuilder("Sub")
                .superclass(ClassName.get("", sup.name))
                .addField(FieldSpec.builder(String.class, "s").addAnnotation(subFieldAnnot).build())
                .build();

        processAndEvaluate(List.of(oneTimeAnnotType, sup, sub), finder -> {
            // Sub.s
            final VariableElement field = finder.findDeclaredField(finder.elements.getTypeElement(sub.name), "s");
            final List<? extends AnnotationMirror> mirrors = finder.getFieldAnnotations(field);
            assertEquals(1, mirrors.size());
            final AnnotationMirror mirror = mirrors.get(0);
            finder.getAnnotationValue(mirror, "value").ifPresentOrElse(
                    v -> assertEquals("hello", v.getValue()),
                    () -> fail("Missing annotation value"));
        });
    }

    // ==================== HELPER METHODS ====================
    /**
     * A convenient method to convert a {@link VariableElement} to a {@link FieldSpec} for further comparison.
     */
    private FieldSpec toFieldSpec(final VariableElement el) {
        return FieldSpec.builder(TypeName.get(el.asType()), el.getSimpleName().toString(), el.getModifiers().toArray(Modifier[]::new))
                .addAnnotations(el.getAnnotationMirrors().stream().map(AnnotationSpec::get).toList())
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

        final Compilation comp = new Compilation(compilationTargets)
                .setCompiler(compiler)
                .setFileManager(fileManager)
                .setOptions(OPTION_PROC_ONLY);
        try {
            final boolean success = comp.compileAndEvaluate(procEnv -> 
                consumer.accept(new ElementFinder(procEnv.getElementUtils(), procEnv.getTypeUtils())));
            assertTrue(success);
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        } finally {
            comp.printDiagnostics();
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