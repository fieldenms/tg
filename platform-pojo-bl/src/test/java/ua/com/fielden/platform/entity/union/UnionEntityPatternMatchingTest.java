package ua.com.fielden.platform.entity.union;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.EntityOne;
import ua.com.fielden.platform.sample.domain.EntityTwo;
import ua.com.fielden.platform.sample.domain.UnionEntity;
import ua.com.fielden.platform.sample.domain.UnionMatchable;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UnionEntityPatternMatchingTest {

    private final Injector injector = new ApplicationInjectorFactory()
            .add(new CommonEntityTestIocModuleWithPropertyFactory())
            .getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void example() {
        var entityOne = factory.newEntity(EntityOne.class, "A", "desc");
        var union = factory.newEntity(UnionEntity.class).setPropertyOne(entityOne);
        switch (union.value()) {
            case EntityOne one -> assertEquals(one, entityOne);
            case EntityTwo two -> fail();
        }
    }

    @Test
    public void matching_with_records() {
        var entityOne = factory.newEntity(EntityOne.class, "A", "desc");
        var union = factory.newEntity(UnionEntity.class).setPropertyOne(entityOne);
        switch (UnionEntity.match1(union)) {
            case UnionEntity.M1.EntityOne (var one) -> assertEquals(one, entityOne);
            case UnionEntity.M1.EntityTwo (var two) -> fail();
            case null -> fail();
        }
    }

    @Test
    public void matching_with_interfaces() {
        var entityOne = factory.newEntity(EntityOne.class, "A", "desc");
        var union = factory.newEntity(UnionEntity.class).setPropertyOne(entityOne);
        switch (UnionEntity.match2(union)) {
            case UnionEntity.M2.EntityOne it -> assertEquals(it.get(), entityOne);
            case UnionEntity.M2.EntityTwo it -> fail();
            case null -> fail();
        }
    }

    @Test
    public void matching_with_UnionMatchable() {
        var entityOne = factory.newEntity(EntityOne.class, "A", "desc");
        var union = factory.newEntity(UnionEntity.class).setPropertyOne(entityOne);
        switch (UnionMatchable.match(union)) {
            case UnionEntity.M1.EntityOne (var one) -> assertEquals(one, entityOne);
            case UnionEntity.M1.EntityTwo (var two) -> fail();
            case null -> fail();
        }
    }

}
