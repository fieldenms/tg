package ua.com.fielden.platform.meta;

import org.junit.Test;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.meta.Assertions.PropertyTypeA;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgReMaxVehicleReading;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class PropertyTypeMetadataGeneratorTest {

    private final TestPropertyMetadataGenerator generator =
            TestPropertyMetadataGenerator.wrap(new PropertyTypeMetadataGenerator());

    @Test
    public void primitive_property_types_are_generated_as_Primitive() {
        Stream.of(String.class, Long.class, Integer.class, BigDecimal.class, Date.class, boolean.class,
                  Colour.class, Hyperlink.class, Currency.class, byte[].class)
                .forEach(klass -> PropertyTypeA.of(generator.generate(klass))
                        .assertIs(PropertyTypeMetadata.Primitive.class)
                        .assertJavaType(klass));
    }

    @Test
    public void DynamicEntityKey_is_generated_as_special_type() {
        PropertyTypeA.of(generator.generate(DynamicEntityKey.class))
                .assertIs(PropertyTypeMetadata.CompositeKey.class);
    }

    @Test
    public void NoKey_is_generated_as_special_type() {
        PropertyTypeA.of(generator.generate(NoKey.class))
                .assertIs(PropertyTypeMetadata.NoKey.class);
    }

    @Test
    public void composite_property_types_are_generated_as_Composite() {
        PropertyTypeA.of(generator.generate(Money.class))
                .assertIs(PropertyTypeMetadata.Component.class)
                .assertJavaType(Money.class);
    }

    @Test
    public void unknown_property_types_are_not_modelled() {
        class A<T> {
            @IsProperty Map<String, String> map;
            @IsProperty List<T> listWithTypeVar;
            @IsProperty Set<? super TgVehicle> superVehicles;
            @IsProperty List rawList;
            @IsProperty List<?> wildList;
            @IsProperty List<T> paramList;
            @IsProperty List<List<String>> nestedList;
        }

        Stream.of("map", "listWithTypeVar", "superVehicles", "rawList", "wildList", "paramList", "nestedList")
                .map(f -> getField(A.class, f))
                .forEach(generator::assertNotGenerated);
    }

    @Test
    public void entity_property_types_are_generated_as_Entity() {
        List.of(TgVehicle.class, TgBogieLocation.class, TgReMaxVehicleReading.class, User.class)
                .forEach(klass -> PropertyTypeA.of(generator.generate(klass))
                        .assertIs(PropertyTypeMetadata.Entity.class)
                        .assertJavaType(klass));
    }

    @Test
    public void collectional_property_types_are_generated_as_Collectional() {
        class A<T> {
            @IsProperty(String.class)
            List<String> strings;
            @IsProperty(TgVehicle.class)
            Set<TgVehicle> vehicles1;
            @IsProperty
            Set<TgVehicle> vehicles2;
            @IsProperty
            Set<? extends TgVehicle> subVehicles;
        }

        final Function<String, PropertyTypeA<PropertyTypeMetadata>> f = fieldName -> fromField(A.class, fieldName);

        f.apply("strings").assertCollectional()
                .assertCollectionType(List.class)
                .elementType().assertIs(PropertyTypeMetadata.Primitive.class);
        f.apply("vehicles1").assertCollectional()
                .assertCollectionType(Set.class)
                .elementType().assertIs(PropertyTypeMetadata.Entity.class).assertJavaType(TgVehicle.class);
        f.apply("vehicles2").assertCollectional()
                .assertCollectionType(Set.class)
                .elementType().assertIs(PropertyTypeMetadata.Entity.class).assertJavaType(TgVehicle.class);
        f.apply("subVehicles").assertCollectional()
                .assertCollectionType(Set.class)
                .elementType().assertIs(PropertyTypeMetadata.Entity.class).assertJavaType(TgVehicle.class);
    }

    @Test
    public void PropertyDescriptor_property_type_is_generated_as_Entity() {
        class A {
            @IsProperty PropertyDescriptor<TgVehicle> pd;
        }

        PropertyTypeA.of(generator.generate(getField(A.class, "pd")))
                .assertIs(PropertyTypeMetadata.Entity.class)
                .assertJavaType(PropertyDescriptor.class);
    }

    @Test
    public void Class_property_type_is_generated_as_primitive() {
        class A {
            @IsProperty Class<? extends ISecurityToken> tokenClass;
            @IsProperty Class rawClass;
        }

        fromField(A.class, "tokenClass").assertIs(PropertyTypeMetadata.Primitive.class).assertJavaType(Class.class);
        fromField(A.class, "rawClass").assertIs(PropertyTypeMetadata.Primitive.class).assertJavaType(Class.class);
    }

    private static Field getField(final Class<?> klass, final String name) {
        try {
            return klass.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private PropertyTypeA<PropertyTypeMetadata> fromField(final Class<?> klass, final String name) {
        return PropertyTypeA.of(generator.generate(getField(klass, name)));
    }

}
