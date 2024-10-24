package ua.com.fielden.platform.audit;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.nio.file.Path;
import java.util.Set;

@ImplementedBy(AuditEntityGeneratorImpl.class)
public interface AuditEntityGenerator {

    default Set<Path> generate(Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes) {
        return generate(entityTypes, Path.of("src/main/java"));
    }

    Set<Path> generate(
            Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes,
            Path sourceRoot);

    // TODO: Display deltas

}
