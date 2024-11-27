package ua.com.fielden.platform.web.interfaces;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Optional;

final class StubEntityMasterUrlProvider implements IEntityMasterUrlProvider {

    @Override
    public Optional<String> masterUrlFor(final AbstractEntity<?> entity) {
        return Optional.empty();
    }

}
