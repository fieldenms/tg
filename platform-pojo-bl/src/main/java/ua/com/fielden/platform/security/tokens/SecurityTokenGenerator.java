package ua.com.fielden.platform.security.tokens;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.ISecurityToken;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.function.Predicate.not;

final class SecurityTokenGenerator implements ISecurityTokenGenerator {

    @Override
    public Class<? extends ISecurityToken> generateToken(
            final Class<? extends AbstractEntity<?>> entityType,
            final Template template)
    {
        final var baseType = PropertyTypeDeterminator.baseEntityType(entityType);

        final var entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(baseType);

        final var tokenFqn = Stream.of(baseType.getPackageName(), template.forClassName().formatted(entityType.getSimpleName()))
                .filter(not(String::isEmpty))
                .collect(Collectors.joining("."));

        final var tokenType = new ByteBuddy()
                .subclass(ISecurityToken.class)
                .name(tokenFqn)
                .modifiers(Visibility.PUBLIC)
                .defineField("TITLE", String.class, Visibility.PUBLIC, Ownership.STATIC, FieldManifestation.FINAL)
                    .value(format(template.forTitle(), entityTitleAndDesc.getKey()))
                .defineField("DESC", String.class, Visibility.PUBLIC, Ownership.STATIC, FieldManifestation.FINAL)
                    .value(format(template.forDesc(), entityTitleAndDesc.getValue()))
                .make()
                .load(baseType.getClassLoader())
                .getLoaded();

        // Ensure that the generated type can be discovered by ClassesRetriever
        ClassesRetriever.registerClass(tokenType);

        return tokenType;
    }

}
