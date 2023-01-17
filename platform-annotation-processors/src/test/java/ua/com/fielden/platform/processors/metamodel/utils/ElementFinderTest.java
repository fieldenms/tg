package ua.com.fielden.platform.processors.metamodel.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.processors.test_utils.Compilation.OPTION_PROC_ONLY;
import static ua.com.fielden.platform.utils.CollectionUtil.isEqualContents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.junit.Assert;
import org.junit.Test;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import ua.com.fielden.platform.processors.test_utils.Compilation;
import ua.com.fielden.platform.processors.test_utils.InMemoryJavaFileManager;
import ua.com.fielden.platform.utils.CollectionUtil;

/**
 * A test case for utility functions in {@link ElementFinder}.
 * <p>
 * Most tests use the javapoet API to dynamically create java sources that will be subject to processing.
 *
 * @author TG Team
 */
public class ElementFinderTest {

    // TODO MetaModelLifecycleTest uses a similar value, so it should be generalized
    private static final TypeSpec PLACEHOLDER = TypeSpec.classBuilder("Placeholder").build();

    public static @interface TestAnnot {
        static final String DEFAULT_VALUE = "default";
        String value() default DEFAULT_VALUE;
    }
    public static @interface EmptyAnnot {}

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
            final TypeElement superclassOfSub = finder.findSuperclass(finder.elements.getTypeElement(sub.name)).orElseThrow();
            assertEquals(finder.elements.getTypeElement(sup.name), superclassOfSub);
            assertOptEquals(finder.getTypeElement(Object.class), finder.findSuperclass(superclassOfSub));
        });
    }

    @Test 
    public void findSuperclass_returns_empty_optional_for_Object() {
        processAndEvaluate(finder -> assertTrue(finder.findSuperclass(finder.getTypeElement(Object.class)).isEmpty()));
    }

    @Test 
    public void findSuperclass_returns_empty_optional_for_an_interface() {
        final TypeSpec isup = TypeSpec.interfaceBuilder("ISup").build();
        final TypeSpec isub = TypeSpec.interfaceBuilder("ISub")
                .addSuperinterface(ClassName.get("", isup.name))
                .build();

        processAndEvaluate(List.of(isup, isub), finder -> {
            assertTrue(finder.findSuperclass(finder.elements.getTypeElement(isup.name)).isEmpty());
            assertTrue(finder.findSuperclass(finder.elements.getTypeElement(isub.name)).isEmpty());
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
            assertEquals(subEl, finder.findField(subEl, "i").orElseThrow().getEnclosingElement());
            // Sup.s
            assertEquals(supEl, finder.findField(subEl, "s", f -> finder.isStatic(f)).orElseThrow().getEnclosingElement());
            // Sup.b
            assertEquals(supEl, finder.findField(subEl, "b").orElseThrow().getEnclosingElement());
            // non-existent field
            assertTrue(finder.findField(subEl, "noSuchField").isEmpty());
            // non-existent field
            assertTrue(finder.findField(subEl, "s", f -> f.getModifiers().contains(Modifier.FINAL)).isEmpty());
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
            final VariableElement field = finder.findDeclaredField(finder.elements.getTypeElement(sub.name), "s").orElseThrow();
            final List<? extends AnnotationMirror> mirrors = finder.getFieldAnnotations(field);
            assertEquals(1, mirrors.size());
            final AnnotationMirror mirror = mirrors.get(0);
            finder.getAnnotationValue(mirror, "value").ifPresentOrElse(
                    v -> assertEquals("hello", v.getValue()),
                    () -> fail("Missing annotation value"));
        });
    }

    @Test
    public void findDeclaredMethods_returns_declared_methods_that_satisfy_predicate() {
        final TypeSpec sup = TypeSpec.classBuilder("Sup")
                // this method should be ignored
                .addMethod(MethodSpec.methodBuilder("supMethod").build())
                .build();

        final MethodSpec m1 = MethodSpec.methodBuilder("m1").addParameter(String.class, "arg").build();
        final MethodSpec m2 = MethodSpec.methodBuilder("m2").build();
        final TypeSpec sub = TypeSpec.classBuilder("Sub")
                .superclass(ClassName.get("", sup.name))
                // constructors should be ignored
                .addMethod(MethodSpec.constructorBuilder().build())
                .addMethods(List.of(m1, m2))
                .addField(int.class, "i")
                .build();

        processAndEvaluate(List.of(sup, sub), finder -> {
            final TypeElement subEl = finder.elements.getTypeElement(sub.name);
            // without any predicate all declared methods are returned
            assertEqualContents(List.of(m1, m2),
                    finder.findDeclaredMethods(subEl).stream().map(m -> toMethodSpec(m)).toList());
            // with a predicate
            final Predicate<ExecutableElement> acceptsArgs = (m -> !m.getParameters().isEmpty());
            assertEqualContents(List.of(m1),
                    finder.findDeclaredMethods(subEl, acceptsArgs).stream().map(m -> toMethodSpec(m)).toList());
        }); 
    }

    @Test
    public void findInheritedMethods_returns_all_methods_inherited_from_superclasses() {
        final MethodSpec ifaceMethod = MethodSpec.methodBuilder("ifaceMethod").addModifiers(Modifier.PUBLIC, Modifier.DEFAULT).build();
        final TypeSpec iface = TypeSpec.interfaceBuilder("IFace")
                // interface declarations should be ignored
                .addMethod(ifaceMethod)
                .build();

        final MethodSpec supIfaceMethod = MethodSpec.methodBuilder("ifaceMethod").build();
        final MethodSpec supMethod = MethodSpec.methodBuilder("supMethod").build();
        final TypeSpec sup = TypeSpec.classBuilder("Sup")
                .addSuperinterface(ClassName.get("", iface.name))
                // constructors should be ignored
                .addMethod(MethodSpec.constructorBuilder().build())
                .addMethods(List.of(supIfaceMethod, supMethod))
                .addField(double.class, "d")
                .build();

        final TypeSpec sub = TypeSpec.classBuilder("Sub")
                .superclass(ClassName.get("", sup.name))
                // declared methods should be ignored
                .addMethod(MethodSpec.methodBuilder("m1").build())
                .addField(int.class, "i")
                .build();

        processAndEvaluate(List.of(iface, sup, sub), finder -> {
            // declared methods of Object should also be found
            final List<MethodSpec> objectMethods = finder.findDeclaredMethods(finder.getTypeElement(Object.class)).stream()
                    .map(m -> toMethodSpec(m)).toList();
            final TypeElement subEl = finder.elements.getTypeElement(sub.name);
            assertEqualContents(concat(objectMethods, List.of(supIfaceMethod, supMethod)),
                    finder.findInheritedMethods(subEl).stream().map(m -> toMethodSpec(m)).toList());
        }); 
    }

    @Test
    public void findMethods_returns_all_methods_with_declared_ones_ordered_first() {
        final MethodSpec ifaceMethod = MethodSpec.methodBuilder("ifaceMethod").addModifiers(Modifier.PUBLIC, Modifier.DEFAULT).build();
        final TypeSpec iface = TypeSpec.interfaceBuilder("IFace")
                // interface declarations should be ignored
                .addMethod(ifaceMethod)
                .build();

        final MethodSpec supIfaceMethod = MethodSpec.methodBuilder("ifaceMethod").build();
        final MethodSpec supMethod = MethodSpec.methodBuilder("supMethod").build();
        final TypeSpec sup = TypeSpec.classBuilder("Sup")
                .addSuperinterface(ClassName.get("", iface.name))
                .addMethods(List.of(supIfaceMethod, supMethod))
                .addField(double.class, "d")
                .build();

        final List<MethodSpec> subMethods = List.of(MethodSpec.methodBuilder("m1").build(), MethodSpec.methodBuilder("m2").build());
        final TypeSpec sub = TypeSpec.classBuilder("Sub")
                .superclass(ClassName.get("", sup.name))
                // these methods should appear first
                .addMethods(subMethods)
                .addField(int.class, "i")
                .build();

        processAndEvaluate(List.of(iface, sup, sub), finder -> {
            // declared methods of Object should also be found
            final List<MethodSpec> objectMethods = finder.findDeclaredMethods(finder.getTypeElement(Object.class)).stream()
                    .map(m -> toMethodSpec(m)).toList();
            final TypeElement subEl = finder.elements.getTypeElement(sub.name);
            final List<MethodSpec> foundMethods = finder.findMethods(subEl).stream().map(m -> toMethodSpec(m)).toList();
            assertTrue(startsWithList(foundMethods, subMethods));
            assertEqualContents(concat(subMethods, List.of(supMethod, supIfaceMethod), objectMethods), foundMethods);
        });
    }

    @Test
    public void findAnnotationMirror_finds_a_directly_present_annotation_of_the_specified_type() {
        // one-time annotation type for diversity
        final TypeSpec oneTimeAnnotType = TypeSpec.annotationBuilder("OneTimeAnnot").build();
        // target annotation
        final AnnotationSpec testAnnot = AnnotationSpec.builder(TestAnnot.class).addMember("value", "$S", "hello").build();
        final TypeSpec example = TypeSpec.classBuilder("Example")
                .addAnnotation(AnnotationSpec.builder(ClassName.get("", oneTimeAnnotType.name)).build())
                .addAnnotation(testAnnot)
                .build();

        processAndEvaluate(List.of(oneTimeAnnotType, example), finder -> {
            final TypeElement el = finder.elements.getTypeElement(example.name);
            assertOptEquals(testAnnot,
                    finder.findAnnotationMirror(el, TestAnnot.class).map(AnnotationSpec::get));
            // try finding a non-existent annotation
            assertTrue(finder.findAnnotationMirror(el, EmptyAnnot.class).isEmpty());
        });
    }

    @Test
    public void getAnnotationValue_returns_the_value_of_the_annotation_element_by_name_if_it_exists() {
        final TypeSpec withValue = TypeSpec.classBuilder("WithValue")
                // @TestAnnot(value = "hello")
                .addAnnotation(AnnotationSpec.builder(TestAnnot.class).addMember("value", "$S", "hello").build())
                .build();
        final TypeSpec withDefaults = TypeSpec.classBuilder("WithDefaults")
                // @TestAnnot
                .addAnnotation(AnnotationSpec.builder(TestAnnot.class).build())
                .build();

        processAndEvaluate(List.of(withValue, withDefaults), finder -> {
            final TypeElement withValueElement = finder.elements.getTypeElement(withValue.name);
            assertOptEquals("hello",
                    finder.findAnnotationMirror(withValueElement, TestAnnot.class).flatMap(a -> 
                        finder.getAnnotationValue(a, "value").map(AnnotationValue::getValue)));

            // default annotation element's value should be returned if none was defined
            final TypeElement withDefaultsElement = finder.elements.getTypeElement(withDefaults.name);
            assertOptEquals(TestAnnot.DEFAULT_VALUE,
                    finder.findAnnotationMirror(withDefaultsElement, TestAnnot.class).flatMap(a -> 
                        finder.getAnnotationValue(a, "value").map(AnnotationValue::getValue)));

            // try obtaining a non-existent element's value
            assertTrue(finder.findAnnotationMirror(withDefaultsElement, TestAnnot.class)
                    .flatMap(a -> finder.getAnnotationValue(a, "whatever")).isEmpty());
        });
    }

    @Test
    public void asType_returns_type_mirror_representation_of_the_given_class() {
        processAndEvaluate(finder -> {
            final Types types = finder.types;
            // primitive type
            final PrimitiveType intType = types.getPrimitiveType(TypeKind.INT);
            assertTrue(types.isSameType(intType, finder.asType(int.class)));
            assertFalse(types.isSameType(intType, finder.asType(Integer.class)));

            // void
            assertTrue(types.isSameType(types.getNoType(TypeKind.VOID), finder.asType(void.class)));

            // array types
            // primitive type array
            assertTrue(types.isSameType(types.getArrayType(intType), finder.asType(int[].class)));
            // declared type array
            final ArrayType stringArrayType = types.getArrayType(finder.asType(String.class));
            assertTrue(types.isSameType(stringArrayType, finder.asType(String[].class)));
            // nested array
            assertTrue(types.isSameType(types.getArrayType(stringArrayType), finder.asType(String[][].class)));

            // declared type
            assertTrue(types.isSameType(finder.getTypeElement(Object.class).asType(), finder.asType(Object.class)));
            // generic type
            assertTrue(types.isSameType(types.getDeclaredType(finder.getTypeElement(List.class)), finder.asType(List.class)));
        });
    }

    @Test
    public void isSameType_returns_true_if_type_mirror_and_class_represent_the_same_type() {
        final TypeSpec example = TypeSpec.classBuilder("Example")
                // <T extends List>
                .addTypeVariable(TypeVariableName.get("T", List.class))
                .addField(Void.class, "v")
                .build();

        processAndEvaluate(List.of(example), finder -> {
            final TypeElement exampleEl = finder.elements.getTypeElement(example.name);

            // primitive type int
            assertTrue(finder.isSameType(finder.types.getPrimitiveType(TypeKind.INT), int.class));
            assertFalse(finder.isSameType(finder.types.getPrimitiveType(TypeKind.INT), Integer.class));
            // void
            assertTrue(finder.isSameType(finder.types.getNoType(TypeKind.VOID), void.class));
            // Void != void
            assertFalse(finder.isSameType(finder.types.getNoType(TypeKind.VOID), Void.class));
            assertTrue(finder.isSameType(finder.findDeclaredField(exampleEl, "v").map(elt -> elt.asType()).orElseThrow(), Void.class));

            // declared types
            final TypeMirror stringType = finder.getTypeElement(String.class).asType();
            assertTrue(finder.isSameType(stringType, String.class));

            // generic type
            final TypeElement listElement = finder.getTypeElement(List.class);
            final TypeMirror stringListType = finder.types.getDeclaredType(listElement, stringType);
            final TypeMirror rawListType = finder.types.getDeclaredType(listElement);
            // List<String> != List
            assertFalse(finder.isSameType(stringListType, List.class));
            // (erased List<String> = List) == List
            assertTrue(finder.isSameType(rawListType, List.class));

            // array type
            final ArrayType stringArrayType = finder.types.getArrayType(stringType);
            assertTrue(finder.isSameType(stringArrayType, String[].class));
            // nested array type
            assertTrue(finder.isSameType(finder.types.getArrayType(stringArrayType), String[][].class));

            // wildcard type: ? extends List
            assertFalse(finder.isSameType(finder.types.getWildcardType(finder.getTypeElement(List.class).asType(), null), ArrayList.class));
            // type variable <T extends List>
            assertFalse(finder.isSameType(exampleEl.getTypeParameters().get(0).asType(), ArrayList.class));
        });
    }

    @Test
    public void getPackageOf_returns_the_package_of_the_element() {
        final TypeSpec example = TypeSpec.classBuilder("Example").build();

        processAndEvaluate(List.of(example), finder -> {
            final TypeElement exampleElt = finder.elements.getTypeElement(example.name);
            final PackageElement examplePkgElt = finder.getPackageOf(exampleElt).orElseThrow();
            assertTrue(examplePkgElt.isUnnamed());
            assertEquals("", examplePkgElt.getQualifiedName().toString());

            assertEquals("java.lang", 
                    finder.getPackageOf(finder.getTypeElement(Object.class)).map(elt -> elt.getQualifiedName().toString()).orElseThrow());
            assertEquals(this.getClass().getPackageName(),
                    finder.getPackageOf(finder.getTypeElement(Nested.class)).map(elt -> elt.getQualifiedName().toString()).orElseThrow());
        });
    }
    // where
    private static class Nested {}

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
     * A convenient method to convert a {@link ExecutableElement} to a {@link MethodSpec} for further comparison.
     */
    private MethodSpec toMethodSpec(final ExecutableElement el) {
        return MethodSpec.methodBuilder(el.getSimpleName().toString())
                .returns(TypeName.get(el.getReturnType()))
                .addParameters(el.getParameters().stream().map(ParameterSpec::get).toList())
                .addExceptions(el.getThrownTypes().stream().map(TypeName::get).toList())
                .addTypeVariables(el.getTypeParameters().stream().map(TypeVariableName::get).toList())
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
            assertTrue("Processing of sources failed.", success);
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

    private <T> boolean startsWithList(final List<T> c1, final List<T> c2) {
        if (c1.size() < c2.size()) {
            return false;
        }
        return c2.equals(c1.subList(0, c2.size()));
    }

    private <T> List<T> concat(final Collection<T>... collections) {
        final ArrayList<T> list = new ArrayList<>(Stream.of(collections).map(Collection::size).reduce(0, (a, b) -> a + b));
        for (Collection<T> c: collections) {
            list.addAll(c);
        }
        return Collections.unmodifiableList(list);
    }

    private <T> void assertOptEquals(final T expected, final Optional<T> maybeActual) {
        maybeActual.ifPresentOrElse(actual -> Assert.assertEquals(expected, actual), () -> fail("Optional is empty."));
    }

}
