package ua.com.fielden.platform.security.tokens;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.security.tokens.Template.EXECUTE;
import static ua.com.fielden.platform.security.tokens.Template.MASTER_OPEN;

import java.util.Optional;
import java.util.stream.Stream;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;

/**
 * A set of utilities for security tokens.
 * 
 * @author TG Team
 *
 */
public class TokenUtils {

    /**
     * Authorises opening of entity master for concrete entity type. Returns result with specific message.
     * 
     * @param entityTypeSimpleName -- simple name of the entity type, opening of which needs authorisation
     * @param authorisation -- model executing authorisation checks
     * @param securityTokenProvider -- security token provider, used to get a token class by name.
     * @return
     */
    public static Result authoriseOpening(final String entityTypeSimpleName, final IAuthorisationModel authorisation, final ISecurityTokenProvider securityTokenProvider) {
        final Optional<Class<ISecurityToken>> maybeToken = Stream
                .of(findToken(entityTypeSimpleName + "Master", MASTER_OPEN, securityTokenProvider),
                    findToken("Open" + entityTypeSimpleName + "MasterAction", MASTER_OPEN, securityTokenProvider),
                    findToken(entityTypeSimpleName, EXECUTE, securityTokenProvider))
                .filter(Optional::isPresent).map(op -> op.get()).findFirst();

        return maybeToken.map(token -> authorisation.authorise(token))
               .orElseGet(() -> failure(format("%s token has not been found for %s.", MASTER_OPEN, entityTypeSimpleName)));
    }

    /**
     * Authorises reading for concrete entity type. Returns result with specific message.
     * 
     * @param entityTypeSimpleName -- simple name of the entity type, reading of which needs authorisation
     * @param readingKind -- {@link Template#READ} or {@link Template#READ_MODEL} kind of reading that needs authorisation
     * @param authorisation -- model executing authorisation checks
     * @param securityTokenProvider -- security token provider, used to get a token class by name.
     * @return
     */
    public static Result authoriseReading(final String entityTypeSimpleName, final Template readingKind, final IAuthorisationModel authorisation, final ISecurityTokenProvider securityTokenProvider) {
        return findToken(entityTypeSimpleName, readingKind, securityTokenProvider)
            .map(Optional::of)
            .orElseGet(() -> findDefaultToken(readingKind, securityTokenProvider))
            .map(token -> authorisation.authorise(token))
            .orElseGet(() -> failure(format("%s token has not been found for %s.", readingKind, entityTypeSimpleName)));
    }

    /**
     * Finds token in {@link ISecurityTokenProvider} for the specified {@code template}, if exists.
     * 
     * @param templateParam -- string param to inject to {@link Template#forClassName()} to form token simple class name
     * @param template -- template (kind) of the token
     * @param securityTokenProvider -- security token provider, used to get a token class by name
     * @return
     */
    public static <T extends ISecurityToken> Optional<Class<T>> findToken(final String templateParam, final Template template, final ISecurityTokenProvider securityTokenProvider) {
        return findTokenFor(of(templateParam), template, securityTokenProvider);
    }

    /**
     * Finds default token, if exists.
     * 
     * @param template -- template (kind) of the token
     * @param securityTokenProvider -- security token provider, used to get a token class by name
     * @return
     */
    public static <T extends ISecurityToken> Optional<Class<T>> findDefaultToken(final Template template, final ISecurityTokenProvider securityTokenProvider) {
        return findTokenFor(empty(), template, securityTokenProvider);
    }

    /**
     * Finds token in {@link ISecurityTokenProvider} for the specified {@code template}, if exists.
     * 
     * @param templateParamOpt -- optional string param to inject to {@link Template#forClassName()} to form token simple class name
     * @param template -- template (kind) of the token
     * @param securityTokenProvider -- security token provider, used to get a token class by name
     * @return
     */
    private static <T extends ISecurityToken>  Optional<Class<T>> findTokenFor(final Optional<String> templateParamOpt, final Template template, final ISecurityTokenProvider securityTokenProvider) {
        final String tokenSimpleClassName = format(template.forClassName(), templateParamOpt.orElse(""));
        return securityTokenProvider.getTokenByName(tokenSimpleClassName);
    }

}