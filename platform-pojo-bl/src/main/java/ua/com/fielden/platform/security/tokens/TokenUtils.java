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
     * @param securityTokensPackageName -- a place where all security tokens are located
     * @param authorisation -- model executing authorisation checks
     * @param securityTokenProvider -- security token provider, used to get a token class by name.
     * @return
     */
    public static Result authoriseReading(final String entityTypeSimpleName, final Template readingKind, final String securityTokensPackageName, final IAuthorisationModel authorisation, final ISecurityTokenProvider securityTokenProvider) {
        return findReadingToken(entityTypeSimpleName, readingKind, securityTokensPackageName, securityTokenProvider)
            .map(Optional::of)
            .orElseGet(() -> findDefaultReadingToken(readingKind, securityTokensPackageName, securityTokenProvider))
            .map(token -> authorisation.authorise(token))
            .orElseGet(() -> failure(format("%s token has not been found for %s.", readingKind, entityTypeSimpleName)));
    }

    /**
     * Finds specific reading token for concrete entity type, if exists.
     * 
     * @param entityTypeSimpleName -- simple name of the entity type
     * @param readingKind -- {@link Template#READ} or {@link Template#READ_MODEL} kind of reading
     * @param securityTokensPackageName -- a place where all security tokens are located
     * @param securityTokenProvider -- security token provider, used to get a token class by name.
     * @return
     */
    public static <T extends ISecurityToken> Optional<Class<T>> findReadingToken(final String entityTypeSimpleName, final Template readingKind, final String securityTokensPackageName, final ISecurityTokenProvider securityTokenProvider) {
        return findReadingTokenFor(of(entityTypeSimpleName), readingKind, securityTokensPackageName, securityTokenProvider);
    }

    /**
     * Finds default reading token, if exists.
     * 
     * @param readingKind -- {@link Template#READ} or {@link Template#READ_MODEL} kind of reading
     * @param securityTokensPackageName -- a place where all security tokens are located
     * @param securityTokenProvider -- security token provider, used to get a token class by name.
     * @return
     */
    public static <T extends ISecurityToken> Optional<Class<T>> findDefaultReadingToken(final Template readingKind, final String securityTokensPackageName, final ISecurityTokenProvider securityTokenProvider) {
        return findReadingTokenFor(empty(), readingKind, securityTokensPackageName, securityTokenProvider);
    }

    /**
     * Finds reading token in packages for 'reading' tokens, if exists.
     * 
     * @param templateParamOpt -- optional string param to inject to {@link Template#forClassName()} to form token class name
     * @param readingKind -- {@link Template#READ} or {@link Template#READ_MODEL} kind of reading
     * @param securityTokensPackageName -- a place where all security tokens are located
     * @return
     */
    private static <T extends ISecurityToken> Optional<Class<T>> findReadingTokenFor(final Optional<String> templateParamOpt, final Template readingKind, final String securityTokensPackageName, final ISecurityTokenProvider securityTokenProvider) {
        return TokenUtils.<T>findReadingTokenFor(templateParamOpt, readingKind, securityTokensPackageName, ".persistent.", securityTokenProvider)
            .map(Optional::of)
            .orElseGet(() -> findReadingTokenFor(templateParamOpt, readingKind, securityTokensPackageName, ".synthetic.", securityTokenProvider));
    }

    /**
     * Finds reading token in concrete package, if exists.
     * 
     * @param templateParamOpt -- optional string param to inject to {@link Template#forClassName()} to form token class name
     * @param readingKind -- {@link Template#READ} or {@link Template#READ_MODEL} kind of reading
     * @param securityTokensPackageName -- a place where all security tokens are located
     * @param packagePart -- package part to be concatenated to {@code securityTokensPackageName}
     * @param securityTokenProvider -- security token provider, used to get a token class by name.
     * @return
     */
    private static <T extends ISecurityToken>  Optional<Class<T>> findReadingTokenFor(final Optional<String> templateParamOpt, final Template readingKind, final String securityTokensPackageName, final String packagePart, final ISecurityTokenProvider securityTokenProvider) {
        final String className = securityTokensPackageName + packagePart + format(readingKind.forClassName(), templateParamOpt.orElse(""));
        return securityTokenProvider.getTokenByName(className);
    }

}