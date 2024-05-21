package ua.com.fielden.platform.meta;

import org.junit.Test;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.meta.Assertions.PropertyTypeA;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgReMaxVehicleReading;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class PropertyTypeMetadataGeneratorTest {

    private final PropertyTypeMetadataGenerator generator = new PropertyTypeMetadataGenerator();

    @Test
    public void primitive_property_types_are_generated_as_Primitive() {
        Stream.of(String.class, Long.class, Integer.class, BigDecimal.class, Date.class, boolean.class,
                  Colour.class, Hyperlink.class, Currency.class, byte[].class)
                .forEach(klass -> PropertyTypeA.of(generator.fromType(klass))
                        .assertIs(PropertyTypeMetadata.Primitive.class)
                        .assertJavaType(klass));
    }

    @Test
    public void DynamicEntityKey_is_generated_as_special_type() {
        PropertyTypeA.of(generator.fromType(DynamicEntityKey.class))
                .assertIs(PropertyTypeMetadata.CompositeKey.class);
    }

    @Test
    public void NoKey_is_generated_as_special_type() {
        PropertyTypeA.of(generator.fromType(NoKey.class))
                .assertIs(PropertyTypeMetadata.NoKey.class);
    }

    @Test
    public void composite_property_types_are_generated_as_Composite() {
        PropertyTypeA.of(generator.fromType(Money.class))
                .assertIs(PropertyTypeMetadata.Composite.class)
                .assertJavaType(Money.class);
    }

    @Test
    public void unknown_property_types_are_generated_as_Other() {
        List.of(Double.class, List.class, Object.class, int.class)
                .forEach(klass -> PropertyTypeA.of(generator.fromType(klass))
                        .assertIs(PropertyTypeMetadata.Other.class)
                        .assertJavaType(klass));
    }

    @Test
    public void entity_property_types_are_generated_as_Entity() {
        List.of(TgVehicle.class, TgBogieLocation.class, TgReMaxVehicleReading.class, User.class)
                .forEach(klass -> PropertyTypeA.of(generator.fromType(klass))
                        .assertIs(PropertyTypeMetadata.Entity.class)
                        .assertJavaType(klass));
    }

    @Test
    public void collectional_property_types_are_generated_as_Collectional() {
        class A<T> {
            List<String> strings;
            Set<TgVehicle> vehicles;
            Set<? extends TgVehicle> subVehicles;
            Set<? super TgVehicle> superVehicles;
            List rawList;
            List<?> wildList;
            List<T> paramList;
            List<List<String>> nestedList;

            static Type fieldType(String name) {
                try {
                    return A.class.getDeclaredField(name).getGenericType();
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        final Function<String, PropertyTypeA<PropertyTypeMetadata>> f =
                fieldName -> PropertyTypeA.of(generator.fromType(A.fieldType(fieldName)));

        f.apply("strings").assertCollectional()
                .assertCollectionType(List.class)
                .elementType().assertIs(PropertyTypeMetadata.Primitive.class);
        f.apply("vehicles").assertCollectional()
                .assertCollectionType(Set.class)
                .elementType().assertIs(PropertyTypeMetadata.Entity.class).assertJavaType(TgVehicle.class);
        f.apply("subVehicles").assertCollectional()
                .assertCollectionType(Set.class)
                .elementType().assertIs(PropertyTypeMetadata.Entity.class).assertJavaType(TgVehicle.class);
        f.apply("superVehicles").assertIs(PropertyTypeMetadata.Other.class);
        f.apply("rawList").assertIs(PropertyTypeMetadata.Other.class);
        f.apply("wildList").assertIs(PropertyTypeMetadata.Other.class);
        f.apply("paramList").assertIs(PropertyTypeMetadata.Other.class);
        f.apply("nestedList").assertIs(PropertyTypeMetadata.Other.class);
    }

}
