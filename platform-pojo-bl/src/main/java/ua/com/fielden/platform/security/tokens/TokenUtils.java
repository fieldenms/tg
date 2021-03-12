package ua.com.fielden.platform.security.tokens;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.error.Result.failure;

import java.util.Optional;

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
     * Authorises reading for concrete entity type. Returns result with specific message.
     * 
     * @param entityTypeSimpleName -- simple name of the entity type, reading of which needs authorisation
     * @param readingKind -- {@link Template#READ} or {@link Template#READ_MODEL} kind of reading that needs authorisation
     * @param authorisation -- model executing authorisation checks
     * @param securityTokenProvider -- security token provider, used to get a token class by name.
     * @return
     */
    public static Result authoriseReading(final String entityTypeSimpleName, final Template readingKind, final IAuthorisationModel authorisation, final ISecurityTokenProvider securityTokenProvider) {
        return findReadingToken(entityTypeSimpleName, readingKind, securityTokenProvider)
            .map(Optional::of)
            .orElseGet(() -> findDefaultReadingToken(readingKind, securityTokenProvider))
            .map(token -> authorisation.authorise(token))
            .orElseGet(() -> failure(format("%s token has not been found for %s.", readingKind, entityTypeSimpleName)));
    }

    /**
     * Finds specific reading token for concrete entity type, if exists.
     * 
     * @param entityTypeSimpleName -- simple name of the entity type
     * @param readingKind -- {@link Template#READ} or {@link Template#READ_MODEL} kind of reading
     * @param securityTokenProvider -- security token provider, used to get a token class by name.
     * @return
     */
    public static <T extends ISecurityToken> Optional<Class<T>> findReadingToken(final String entityTypeSimpleName, final Template readingKind, final ISecurityTokenProvider securityTokenProvider) {
        return findReadingTokenFor(of(entityTypeSimpleName), readingKind, securityTokenProvider);
    }

    /**
     * Finds the default reading token, if exists.
     * 
     * @param readingKind -- {@link Template#READ} or {@link Template#READ_MODEL} kind of reading
     * @param securityTokenProvider -- security token provider, used to get a token class by name.
     * @return
     */
    public static <T extends ISecurityToken> Optional<Class<T>> findDefaultReadingToken(final Template readingKind, final ISecurityTokenProvider securityTokenProvider) {
        return findReadingTokenFor(empty(), readingKind, securityTokenProvider);
    }

    /**
     * Finds reading token in packages for READ and READ_MODEL tokens, if exists.
     * 
     * @param templateParamOpt -- optional string param to inject to {@link Template#forClassName()} to form token class name
     * @param readingKind -- {@link Template#READ} or {@link Template#READ_MODEL} kind of reading
     * @param securityTokensPackageName -- a place where all security tokens are located
     * @return
     */
    private static <T extends ISecurityToken>  Optional<Class<T>> findReadingTokenFor(final Optional<String> templateParamOpt, final Template readingKind, final ISecurityTokenProvider securityTokenProvider) {
        final String simpleClassName = format(readingKind.forClassName(), templateParamOpt.orElse(""));
        return securityTokenProvider.getTokenByName(simpleClassName);
    }

}