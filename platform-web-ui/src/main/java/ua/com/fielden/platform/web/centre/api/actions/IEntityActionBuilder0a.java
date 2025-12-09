package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.tiny.IActionIdentifier;

import java.util.Optional;

public interface IEntityActionBuilder0a<T extends AbstractEntity<?>> {

    IEntityActionBuilder0b<T> withTinyHyperlink(IActionIdentifier actionIdentifier);

    IEntityActionBuilder0b<T> withTinyHyperlink(Optional<IActionIdentifier> maybeActionIdentifier);

}
