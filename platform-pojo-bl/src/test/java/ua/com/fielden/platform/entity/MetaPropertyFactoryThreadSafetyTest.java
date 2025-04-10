package ua.com.fielden.platform.entity;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.fail;

public class MetaPropertyFactoryThreadSafetyTest {

    private final Injector injector = new ApplicationInjectorFactory()
            .add(new CommonEntityTestIocModuleWithPropertyFactory())
            .getInjector();

    @Test
    public void concurrent_entity_instantiation_is_supported() {
        try (var executor = Executors.newThreadPerTaskExecutor(Executors.defaultThreadFactory())) {
            final Callable<TgTimesheet> task = () ->  injector.getInstance(TgTimesheet.class);

            executor.invokeAll(Collections.nCopies(10, task)).forEach(MetaPropertyFactoryThreadSafetyTest::waitForResult);
        } catch (final Exception ex) {
            fail("No exception was expected: %s".formatted(ex.getMessage()));
        }
    }

    static <V> void waitForResult(final Future<V> future) {
        try {
            future.get();
        } catch (final Exception ex) {
            throw new EntityException("Could not instantiate entity.", ex);
        }
    }

}