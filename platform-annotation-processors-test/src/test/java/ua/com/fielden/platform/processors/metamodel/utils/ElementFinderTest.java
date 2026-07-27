package ua.com.fielden.platform.processors.metamodel.utils;

import com.squareup.javapoet.*;
import org.junit.Assert;
import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.exceptions.ElementFinderException;
import ua.com.fielden.platform.processors.test_utils.Compilation;
import ua.com.fielden.platform.processors.test_utils.CompilationResult;
import ua.com.fielden.platform.processors.test_utils.exceptions.CompilationException;
import ua.com.fielden.platform.utils.CollectionUtil;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.processors.test_utils.Compilation.OPTION_PROC_ONLY;
import static ua.com.fielden.platform.utils.CollectionUtil.areEqualByContents;

/**
 * A test case for utility functions in {@link ElementFinder}.
 * <p>
 * Most tests use the javapoet API to dynamically create java sources that will be subject to processing.
 *
 * @author TG Team
 */
public class ElementFinderTest {

    private static final TypeSpec PLACEHOLDER = TypeSpec.classBuilder("Placeholder").build();

    public static @interface TestAnnot {
        static final String DEFAULT_VALUE = "default";
        String value() default DEFAULT_VALUE;
    }
    public static @interface EmptyAnnot {}

    @Test
    public void isSameType_tests_whether_type_element_and_class_represent_the_same_type() {
        processAndEvaluate(finder -> {
            assertTrue(ElementFinder.isSameType(finder.getTypeElement(String.class), String.class));
            assertTrue(ElementFinder.isSameType(finder.getTypeElement(Date.class), Date.class));
            // java.sql.Date != java.util.Date
            assertFalse(ElementFinder.isSameType(finder.getTypeElement(java.sql.Date.class), Date.class));
            // java.util.Date != java.sql.Date
            assertFalse(ElementFinder.isSameType(finder.getTypeElement(Date.class), java.sql.Date.class));
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
            final TypeElement superclassOfSub = ElementFinder.findSuperclass(finder.elements.getTypeElement(sub.name)).orElseThrow();
            assertEquals(finder.elements.getTypeElement(sup.name), superclassOfSub);
            assertOptEquals(finder.getTypeElement(Object.class), ElementFinder.findSuperclass(superclassOfSub));
        });
    }

    @Test 
    public void findSuperclass_returns_empty_optional_for_Object() {
        processAndEvaluate(finder -> assertTrue(ElementFinder.findSuperclass(finder.getTypeElement(Object.class)).isEmpty()));
    }

    @Test 
    public void findSuperclass_returns_empty_optional_for_an_interface() {
        final TypeSpec isup = TypeSpec.interfaceBuilder("ISup").build();
        final TypeSpec isub = TypeSpec.interfaceBuilder("ISub")
                .addSuperinterface(ClassName.get("", isup.name))
                .build();

        processAndEvaluate(List.of(isup, isub), finder -> {
            assertTrue(ElementFinder.findSuperclass(finder.elements.getTypeElement(isup.name)).isEmpty());
            assertTrue(ElementFinder.findSuperclass(finder.elements.getTypeElement(isub.name)).isEmpty());
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

    @Test
    public void findSuperclassesBelow_returns_an_ordered_hierarchy_of_superclasses_below_the_root_type() {
        processAndEvaluate(finder -> {
            // superclassesBelow of Sub (root = Object) = [Sup]
            assertEquals(Stream.of(FindSuperclasses_Sup.class).map(c -> finder.getTypeElement(c)).toList(),
                    finder.findSuperclassesBelow(finder.getTypeElement(FindSuperclasses_Sub.class), Object.class));
            // superclassesBelow of Sub (root = Sup) = []
            assertEquals(List.of(), 
                    finder.findSuperclassesBelow(finder.getTypeElement(FindSuperclasses_Sub.class), FindSuperclasses_Sup.class));
            // superclassesBelow of Sub (root = Sub) = []
            assertEquals(List.of(), 
                    finder.findSuperclassesBelow(finder.getTypeElement(FindSuperclasses_Sub.class), FindSuperclasses_Sub.class));
            // unrelated hierarchies result into an empty list
            assertEquals(List.of(), finder.findSuperclassesBelow(finder.getTypeElement(FindSuperclasses_Sub.class), String.class));
        });
    }
    // where
    private static interface FindSuperclasses_IFace {}
    private static class FindSuperclasses_Sup implements FindSuperclasses_IFace {}
    private static class FindSuperclasses_Sub extends FindSuperclasses_Sup {}

    @Test
    public void streamAllSupertypes_returns_a_stream_of_all_supertypes() {
        processAndEvaluate(finder -> {
            final BiConsumer<Class<?>, Collection<Class<?>>> assertor = (type, expectedSupertypes) -> {
                assertEquals(expectedSupertypes.stream().map(finder::getTypeElement).collect(Collectors.toSet()),
                        finder.streamAllSupertypes(finder.getTypeElement(type)).collect(Collectors.toSet()));
            };

            assertor.accept(HashSet.class,
                    List.of(AbstractSet.class, Set.class, Cloneable.class, Serializable.class, AbstractCollection.class, Collection.class,
                            Iterable.class, Object.class));
            assertor.accept(Object.class, List.of());
            assertor.accept(AbstractEntity.class, List.of(Comparable.class, Object.class));
        });
    }

    @Test
    public void findDeclaredFields_returns_only_declared_fields() {
        final TypeSpec sup = TypeSpec.classBuilder("Sup")
                .addField(String.class, "s", Modifier.PUBLIC)
                .build();
        final TypeSpec sub = TypeSpec.classBuilder("Sub")
                .superclass(ClassName.get("", sup.name))
                .addField(int.class, "i")
                .addField(boolean.class, "b")
                .build();

        processAndEvaluate(List.of(sup, sub), finder -> {
            final List<VariableElement> fields = finder.findDeclaredFields(finder.getTypeElement(sub.name));
            assertEquals(2, fields.size());
            assertEqualContents(sub.fieldSpecs, fields.stream().map(f -> toFieldSpec(f)).toList());
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
            assertEquals(supEl, finder.findField(subEl, "s", f -> ElementFinder.isStatic(f)).orElseThrow().getEnclosingElement());
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
            finder.findAnnotationValue(mirror, "value").ifPresentOrElse(
                    v -> assertEquals("hello", v.getValue()),
                    () -> fail("Missing annotation value"));
        });
    }

    @Test
    public void findDeclaredMethods_finds_only_declared_methods() {
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
            assertEqualContents(List.of(m1, m2),
                    finder.findDeclaredMethods(subEl).stream().map(m -> toMethodSpec(m)).toList());
        }); 
    }

    @Test
    public void findInheritedMethods_returns_all_methods_inherited_from_superclasses() {
        final MethodSpec ifaceMethod = MethodSpec.methodBuilder("ifaceMethod").addModifiers(Modifier.PUBLIC, Modifier.DEFAULT).build();
        final TypeSpec iface = TypeSpec.interfaceBuilder("IFace")
                // interface declarations should be ignored
                .addMethod(ifaceMethod)
                .build();

        final TypeSpec sup1 = TypeSpec.classBuilder("Sup1")
                .addSuperinterface(ClassName.get("", iface.name))
                .addMethod(MethodSpec.methodBuilder(ifaceMethod.name).build()) // implements the interface
                .addMethod(MethodSpec.methodBuilder("superMethod").build())
                .addField(double.class, "d")
                .build();

        final TypeSpec sup2 = TypeSpec.classBuilder("Sup2")
                .superclass(ClassName.get("", sup1.name))
                // constructors should be ignored
                .addMethod(MethodSpec.constructorBuilder().build())
                // overrides Sup1#superMethod()
                .addMethod(MethodSpec.methodBuilder("superMethod").build())
                .build();

        final TypeSpec sub = TypeSpec.classBuilder("Sub")
                .superclass(ClassName.get("", sup2.name))
                // declared methods should be ignored
                .addMethod(MethodSpec.methodBuilder("method").build())
                .addField(int.class, "i")
                .build();

        processAndEvaluate(List.of(iface, sup1, sup2, sub), finder -> {
            // declared methods of Object should also be found
            final List<String> objectMethods = finder.findDeclaredMethods(finder.getTypeElement(Object.class)).stream()
                    .map(m -> m.getSimpleName().toString()).toList();

            final TypeElement subEl = finder.getTypeElement(sub.name);
            // superMethod is declared by Sup1 and Sup2
            assertEqualContents(concat(objectMethods, List.of("superMethod", "superMethod", ifaceMethod.name)),
                    finder.findInheritedMethods(subEl).stream().map(m -> m.getSimpleName().toString()).toList());
        }); 
    }

    @Test
    public void findMethods_returns_all_methods_with_declared_ones_ordered_first() {
        final MethodSpec ifaceMethod = MethodSpec.methodBuilder("ifaceMethod").addModifiers(Modifier.PUBLIC, Modifier.DEFAULT).build();
        final TypeSpec iface = TypeSpec.interfaceBuilder("IFace")
                // interface declarations should be ignored
                .addMethod(ifaceMethod)
                .build();

        final TypeSpec sup = TypeSpec.classBuilder("Sup")
                .addSuperinterface(ClassName.get("", iface.name))
                .addMethod(MethodSpec.methodBuilder(ifaceMethod.name).build()) // implements the interface
                .addMethod(MethodSpec.methodBuilder("superMethod").build())
                .addField(double.class, "d")
                .build();

        final TypeSpec sub = TypeSpec.classBuilder("Sub")
                .superclass(ClassName.get("", sup.name))
                .addMethod(MethodSpec.methodBuilder("method").build())
                .addMethod(MethodSpec.methodBuilder("superMethod").build()) // overrides Sup#superMethod()
                .addField(int.class, "i")
                .build();

        processAndEvaluate(List.of(iface, sup, sub), finder -> {
            // declared methods of Object should also be found
            final List<String> objectMethods = finder.findDeclaredMethods(finder.getTypeElement(Object.class)).stream()
                    .map(m -> m.getSimpleName().toString()).toList();

            final TypeElement subEl = finder.getTypeElement(sub.name);
            final List<String> foundMethods = finder.findMethods(subEl).stream().map(m -> m.getSimpleName().toString()).toList();
            assertTrue(startsWithList(foundMethods, List.of("method", "superMethod")));
            // superMethod is declared by Sub and Sup
            assertEqualContents(concat(List.of("method", "superMethod", "superMethod", ifaceMethod.name), objectMethods), foundMethods);
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
    public void getAnnotationElementValue_returns_the_value_of_the_annotation_element_by_name_if_it_exists() {
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
            final AnnotationMirror annotMirrorWithValue = finder.findAnnotationMirror(withValueElement, TestAnnot.class).get();
            assertEquals("hello", finder.<String> getAnnotationElementValue(annotMirrorWithValue, "value"));

            // default value should be returned if none was explicitly specified
            final TypeElement withDefaultsElement = finder.elements.getTypeElement(withDefaults.name);
            final AnnotationMirror annotMirrorWithDefaults = finder.findAnnotationMirror(withDefaultsElement, TestAnnot.class).get();
            assertEquals(TestAnnot.DEFAULT_VALUE, finder.<String> getAnnotationElementValue(annotMirrorWithDefaults, "value"));
            // obtaining a non-existent element's value throws
            assertThrows(ElementFinderException.class, () -> finder.getAnnotationElementValue(annotMirrorWithDefaults, "whatever"));
        });
    }

    @Test
    public void getAnnotationElementValueOfClassType_returns_the_type_mirror_representing_the_class_used_as_annotation_element_value() {
        // @AnnotWithClassValue(List.class) class Example {}
        final TypeSpec example = TypeSpec.classBuilder("Example")
                .addAnnotation(AnnotationSpec.builder(AnnotWithClassValue.class).addMember("value", "$T.class", List.class).build())
                .build();

        processAndEvaluate(List.of(example), finder -> {
            final TypeElement exampleElt = finder.elements.getTypeElement(example.name);
            final AnnotWithClassValue annot = exampleElt.getAnnotation(AnnotWithClassValue.class);
            assertNotNull(annot);
            final TypeMirror typeMirror = finder.getAnnotationElementValueOfClassType(annot, a -> a.value());
            assertTrue(finder.isSameType(typeMirror, List.class));
        });
    }
    // where
    public static @interface AnnotWithClassValue {
        Class<?> value();
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
    public void isSameType_returns_true_if_type_mirror_and_class_represent_the_same_raw_type() {
        // class Example<T extends List>
        final TypeSpec example = TypeSpec.classBuilder("Example")
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
            // List<String> == List
            assertTrue(finder.isSameType(stringListType, List.class));
            // (raw List<String> = List) == List
            assertTrue(finder.isSameType(rawListType, List.class));

            // array type
            final ArrayType stringArrayType = finder.types.getArrayType(stringType);
            assertTrue(finder.isSameType(stringArrayType, String[].class));
            // nested array type
            assertTrue(finder.isSameType(finder.types.getArrayType(stringArrayType), String[][].class));
        });
    }

    @Test
    public void isSameType_returns_false_for_wildcard_types() {
        processAndEvaluate(finder -> {
            // ? extends Number
            final WildcardType extendsNumber = finder.types.getWildcardType(finder.getTypeElement(Number.class).asType(), null);
            assertFalse(finder.isSameType(extendsNumber, Number.class));
        });
    }

    @Test
    public void isSameType_returns_false_for_type_variables() {
        // class Example<T extends Number>
        final TypeSpec example = TypeSpec.classBuilder("Example")
                .addTypeVariable(TypeVariableName.get("T", Number.class))
                .build();

        processAndEvaluate(List.of(example), finder -> {
            final TypeElement exampleElt = finder.getTypeElement(example.name);
            // T extends Number
            final TypeVariable typeVar = (TypeVariable) exampleElt.getTypeParameters().get(0).asType();
            assertFalse(finder.isSameType(typeVar, Number.class));
        });
    }

    @Test
    public void isSubtype_any_type_is_considered_to_be_a_subtype_of_itself() {
        processAndEvaluate(finder -> {
            final PrimitiveType intType = finder.types.getPrimitiveType(TypeKind.INT); 
            // primitive type
            assertTrue(finder.isSubtype(intType, int.class));
            // primitive array type
            assertTrue(finder.isSubtype(finder.types.getArrayType(intType), int[].class));
            // array type
            assertTrue(finder.isSubtype(finder.types.getArrayType(finder.asType(String.class)), String[].class));
            // void
            assertTrue(finder.isSubtype(finder.types.getNoType(TypeKind.VOID), void.class));
            // declared type
            assertTrue(finder.isSubtype(finder.getTypeElement(String.class).asType(), String.class));
            // List<String> and List
            final DeclaredType stringList = finder.types.getDeclaredType(
                    finder.getTypeElement(List.class), finder.getTypeElement(String.class).asType());
            assertTrue(finder.isSubtype(stringList, List.class));
        });
    }

    @Test
    public void isSubtype_handles_array_types() {
        processAndEvaluate(finder -> {
            final ArrayType integerArray = finder.types.getArrayType(finder.getTypeElement(Integer.class).asType());
            assertTrue(finder.isSubtype(integerArray, Number[].class));
            final ArrayType integerArray2D = finder.types.getArrayType(integerArray);
            assertFalse(finder.isSubtype(integerArray2D, Number[].class));
            assertTrue(finder.isSubtype(integerArray2D, Number[][].class));
            assertFalse(finder.isSubtype(integerArray2D, Number[][][].class));
        });
    }

    @Test
    public void isSubtype_handles_declared_types() {
        processAndEvaluate(finder -> {
            assertTrue(finder.isSubtype(finder.getTypeElement(Integer.class).asType(), Number.class));

            // parameterised type
            final DeclaredType stringArrayList = finder.types.getDeclaredType(
                    finder.getTypeElement(ArrayList.class), finder.getTypeElement(String.class).asType());
            assertTrue(finder.isSubtype(stringArrayList, List.class));

            assertFalse(finder.isSubtype(finder.getTypeElement(Integer.class).asType(), List.class));
        });
    }

    @Test
    public void isSubtype_returns_false_for_wildcard_types() {
        processAndEvaluate(finder -> {
            // ? extends Number
            final WildcardType extendsNumber = finder.types.getWildcardType(finder.getTypeElement(Number.class).asType(), null);
            assertFalse(finder.isSubtype(extendsNumber, Number.class));
        });
    }

    @Test
    public void isSubtype_returns_false_for_type_variables() {
        // class Example<T extends Number>
        final TypeSpec example = TypeSpec.classBuilder("Example")
                .addTypeVariable(TypeVariableName.get("T", Number.class))
                .build();

        processAndEvaluate(List.of(example), finder -> {
            final TypeElement exampleElt = finder.getTypeElement(example.name);
            // T extends Number
            final TypeVariable typeVar = (TypeVariable) exampleElt.getTypeParameters().get(0).asType();
            assertFalse(finder.isSubtype(typeVar, Number.class));
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
     * @throws CompilationException if an exception was thrown during annotation processing
     */
    private void processAndEvaluate(final Collection<TypeSpec> typeSpecs, final Consumer<ElementFinder> consumer) {
        final List<JavaFileObject> compilationTargets = typeSpecs.stream()
                .map(ts -> JavaFile.builder(/*packageName*/ "", ts).build().toJavaFileObject())
                .toList();
        final Compilation comp = Compilation.newInMemory(compilationTargets).addOptions(OPTION_PROC_ONLY);

        try {
            final CompilationResult result = comp.compileAndEvaluate(procEnv ->
                consumer.accept(new ElementFinder(procEnv)));
            // TODO print diagnostics in case of failure
            assertTrue("Processing of sources failed.", result.success());
        } catch (final Throwable t) {
            throw new CompilationException(t);
        }
    }

    /**
     * Similar to {@link #processAndEvaluate(Collection, Consumer)}, but requires no input sources.
     */
    private void processAndEvaluate(final Consumer<ElementFinder> consumer) {
        processAndEvaluate(List.of(PLACEHOLDER), consumer);
    }

    private static void assertEqualContents(final Collection<?> c1, final Collection<?> c2) {
        if (areEqualByContents(c1, c2)) {}
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
