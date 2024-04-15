package ua.com.fielden.platform.web.interfaces;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Optional;

public interface IUriGenerator {

    <T extends AbstractEntity<?>> Optional<String> generateUri(T entity);
}
