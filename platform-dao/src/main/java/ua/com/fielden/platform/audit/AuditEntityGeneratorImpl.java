package ua.com.fielden.platform.audit;

import com.google.common.collect.Streams;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

final class AuditEntityGeneratorImpl implements AuditEntityGenerator {

    static final String A3T = "a3t";

    private final IDomainMetadata domainMetadata;

    @Inject
    AuditEntityGeneratorImpl(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
    }

    @Override
    public Set<Path> generate(
            final Iterable<? extends Class<? extends AbstractEntity<?>>> entityTypes,
            final Path sourceRoot)
    {
        return Streams.stream(entityTypes)
                .parallel()
                .map(type -> generate_(type, sourceRoot))
                .collect(toImmutableSet());
    }

    private Path generate_(final Class<? extends AbstractEntity<?>> type, final Path sourceRoot) {
        final var auditTypePkg = type.getPackageName();
        // TODO multiple audit-entity versions
        final var auditTypeVersion = 1;
        final var auditTypeName = type.getSimpleName() + "_" + A3T + "_" + auditTypeVersion;
        final var auditTypePath = sourceRoot.resolve(classNameToFilePath(auditTypePkg, auditTypeName));

        try {
            generateSource(type, auditTypePkg, auditTypeName, sourceRoot);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to generate audit-entity (version: %s) for [%s]".formatted(auditTypeVersion, type.getTypeName()), e);
        }

        return auditTypePath;
    }

    private void generateSource(
            final Class<? extends AbstractEntity<?>> type,
            final String auditTypePkg,
            final String auditTypeName,
            final Path sourceRoot)
        throws IOException
    {
        final var typeSpec = TypeSpec.classBuilder(ClassName.get(auditTypePkg, auditTypeName))
                .build();
        final var javaFile = JavaFile.builder(auditTypePkg, typeSpec)
                .build();
        javaFile.writeTo(sourceRoot);
    }

    private static Path classNameToFilePath(final String packageName, final String classSimpleName) {
        return Path.of(packageName.replace('.', '/'), classSimpleName + ".java");
    }

}
