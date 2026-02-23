package ua.com.fielden.platform.web.utils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.either.Either;

import java.util.List;

public interface EntityCentreAPI {

    <T extends AbstractEntity<?>> Either<Result, List<T>> entityCentreResult(
        String configUuid
    );

    Either<Result, Boolean> entityCentreResultExists(
        String configUuid
    );

}
