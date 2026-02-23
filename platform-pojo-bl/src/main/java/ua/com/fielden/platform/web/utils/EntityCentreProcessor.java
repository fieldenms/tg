package ua.com.fielden.platform.web.utils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.either.Either;

import java.util.List;

public interface EntityCentreProcessor {

    <T extends AbstractEntity<?>> Either<Result, List<T>> getResult(
        String configUuid
    );

    Either<Result, Boolean> resultExists(
        String configUuid
    );

}
