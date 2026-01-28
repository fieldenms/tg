package ua.com.fielden.platform.test;

import jakarta.inject.Singleton;
import ua.com.fielden.platform.companion.ICompanionGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Stub implementation of entity companion generation for tests in the {@code platform-pojo-bl} module.
 * It is needed to satisfy the dependent components, such as entity companion finder implementation.
 * <p>
 * This implementation always throws an exception, as the generation of companions can only be implemented in the
 * {@code platform-dao} module.
 * Therefore, if this facility is required in tests, the tests should be put in the {@code platform-dao} module and the
 * IoC module that binds the real implementation should be used.
 */
@Singleton
public final class CompanionGeneratorStub implements ICompanionGenerator {

    @Override
    public Class<?> generateCompanion(final Class<? extends AbstractEntity> type) {
        throw new UnsupportedOperationException();
    }

}
