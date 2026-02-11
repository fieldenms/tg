package ua.com.fielden.platform.security.tokens;

import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotationOptionally;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.stripIfNeeded;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;
import static ua.com.fielden.platform.security.tokens.Template.EXECUTE;
import static ua.com.fielden.platform.security.tokens.Template.MASTER_OPEN;

/// A set of utilities for security tokens.
///
public class TokenUtils {

    public static final String ERR_TOKEN_NOT_FOUND = "%s token has not been found for %s.";

    /// Authorises opening of entity master for concrete entity type. Returns result with specific message.
    ///
    /// @param entityTypeSimpleName  a simple name of the entity type, opening of which needs authorisation
    /// @param authorisation         a model executing authorisation checks
    /// @param securityTokenProvider a security token provider, used to get a token class by name.
    /// 
    public static Result authoriseOpening(final String entityTypeSimpleName, final IAuthorisationModel authorisation, final ISecurityTokenProvider securityTokenProvider) {
        final Optional<Class<ISecurityToken>> maybeToken = Stream
                .of(findToken(entityTypeSimpleName + "Master", MASTER_OPEN, securityTokenProvider),
                    findToken("Open" + entityTypeSimpleName + "MasterAction", MASTER_OPEN, securityTokenProvider),
                    findToken(entityTypeSimpleName, EXECUTE, securityTokenProvider))
                .filter(Optional::isPresent).map(Optional::get).findFirst();

        return maybeToken.map(authorisation::authorise)
               .orElseGet(() -> failure(ERR_TOKEN_NOT_FOUND.formatted(MASTER_OPEN, entityTypeSimpleName)));
    }

    /// Authorises reading for an entity type.
    /// Returns [Result] with a specific message.
    ///
    /// @param entityTypeSimpleName  a simple name of the entity type, reading of which needs authorisation
    /// @param readingKind           either [#READ] or [#READ_MODEL] kind of reading that needs authorisation
    /// @param authorisation         a model executing authorisation checks
    /// @param securityTokenProvider a security token provider, used to get a token class by name.
    ///
    public static Result authoriseReading(final String entityTypeSimpleName, final Template readingKind, final IAuthorisationModel authorisation, final ISecurityTokenProvider securityTokenProvider) {
        return findToken(entityTypeSimpleName, readingKind, securityTokenProvider)
               .or(() -> findDefaultToken(readingKind, securityTokenProvider))
               .map(authorisation::authorise)
               .orElseGet(() -> failure(format(ERR_TOKEN_NOT_FOUND, readingKind, entityTypeSimpleName)));
    }

    /// Validates the criteria values to determine whether they can be applied under the authorisation model.
    ///
    public static Result authoriseCriteria(final List<QueryProperty> queryProperties, final IAuthorisationModel authorisation) {
        return queryProperties.stream()
            .map(queryProperty -> {
                // Criterion without any conditions is authorised.
                // Root property (aka "entity itself") is always authorised.
                if (!queryProperty.isEmptyWithoutMnemonics() && !"".equals(queryProperty.getPropertyName())) {
                    final var originalType = getOriginalType(stripIfNeeded(queryProperty.getEntityClass()));
                    return authorisePropertyReading(originalType, queryProperty.getPropertyName(), authorisation);
                }
                return Optional.<Result>empty();
            })
            .flatMap(Optional::stream)
            .filter(res -> !res.isSuccessful())
            .findFirst()
            .orElseGet(Result::successful);
    }

    public static Optional<Result> authorisePropertyReading(final Class<?> entityType, final String propertyName, final IAuthorisationModel authorisation) {
        return getPropertyAnnotationOptionally(Authorise.class, entityType, propertyName)
               .map(annot -> (Class<? extends ISecurityToken>) annot.value())
               .map(authorisation::authorise);
    }

    /// Finds token in [ISecurityTokenProvider] for the specified `template`, if exists.
    ///
    /// @param templateParam         a string param to inject into [#forClassName()] to form token's simple class name
    /// @param template              a security token template
    /// @param securityTokenProvider a security token provider, used to get a token class by name
    ///
    public static <T extends ISecurityToken> Optional<Class<T>> findToken(final String templateParam, final Template template, final ISecurityTokenProvider securityTokenProvider) {
        return findTokenFor(of(templateParam), template, securityTokenProvider);
    }

    /// Finds default token, if exists.
    ///
    /// @param template              a security token template
    /// @param securityTokenProvider a security token provider, used to get a token class by name
    ///
    public static <T extends ISecurityToken> Optional<Class<T>> findDefaultToken(final Template template, final ISecurityTokenProvider securityTokenProvider) {
        return findTokenFor(empty(), template, securityTokenProvider);
    }

    /// Finds token in [ISecurityTokenProvider] for the specified `template`, if exists.
    ///
    /// @param templateParamOpt      an optional string param to inject to [#forClassName()] to form token's simple class name
    /// @param template              a security token template
    /// @param securityTokenProvider a security token provider, used to get a token class by name
    /// @return
    private static <T extends ISecurityToken>  Optional<Class<T>> findTokenFor(final Optional<String> templateParamOpt, final Template template, final ISecurityTokenProvider securityTokenProvider) {
        final String tokenSimpleClassName = format(template.forClassName(), templateParamOpt.orElse(""));
        return securityTokenProvider.getTokenByName(tokenSimpleClassName);
    }

}