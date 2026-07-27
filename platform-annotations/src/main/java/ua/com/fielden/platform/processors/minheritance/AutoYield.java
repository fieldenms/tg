package ua.com.fielden.platform.processors.minheritance;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/// Annotates a property of a specification entity type (annotated with [ua.com.fielden.platform.entity.annotation.Extends])
/// to indicate that the yields for the property should be inserted automatically when an EQL model is generated for the specification entity type.
///
/// A typical use case for this, is to redeclare an inherited property with a custom title and description.
///
/// For example:
///
/// ```java
/// @Extends(value = {@Entity(Equipment.class), @Entity(Building.class)}, name = "ReAsset")
/// class ReAsset_Spec extends AbstractEntity<NoKey> {
///     @IsProperty
///     @Title("Asset Number")
///     @AutoYield
///     String number;
///
///     ... modelFor(...) { ... }
/// }
///
/// class Equipment extends AbstractEntity<...> {
///     @IsProperty
///     @MapTo
///     @Title("Equipment Number")
///     String number;
/// }
///
/// class Building extends AbstractEntity<...> {
///     @IsProperty
///     @MapTo
///     @Title("Building Number")
///     String number;
/// }
/// ```
///
/// Due to `@AutoYield` on `ReAsset_Spec.number`, the EQL model generator will ensure that the resulting model contains
/// the following:
///
/// ```java
/// select(Building.class)
/// ...
/// yield().prop("number").as().prop("number")
/// ...
///
/// select(Equipment.class)
/// ...
/// yield().prop("number").as().prop("number")
/// ...
///
/// ```
///
/// Also, the generated `ReAsset` will not declare property `number`, inheriting it from `ReAsset_Spec` instead.
///
/// It is an error if the annotated property is not inherited from any of the types in `@Extends`, which could be due to
/// the property being excluded or simply absent in the extended type.
///
@Target(FIELD)
@Retention(RUNTIME)
public @interface AutoYield {}
