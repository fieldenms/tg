package ua.com.fielden.platform.security;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.types.try_wrapper.TryWrapper.Try;

import java.lang.reflect.Field;
import java.util.Optional;

import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.CollectionUtil;

/**
 * A convenient utility class for obtaining static security tone information such as key, description, super token etc.
 * 
 * @author TG Team
 * 
 */
public class SecurityTokenInfoUtils {
    public static final String ERR_MISSING_TOKEN = "Token is required.";
    public static final String ERR_MELFORMED_TOKEN = "Security token %s is malformed: either KeyTitle annotation or static TITLE/DESC fields are expected.";

    private SecurityTokenInfoUtils() {
    }

    /**
     * Determines security token's title, which represents its short description.
     * 
     * @param token
     * @return
     */
    public static String shortDesc(final Class<? extends ISecurityToken> token) {
        return securityTokenPart(token, TokenPart.TITLE);
    }

    /**
     * Determines security token's description, which represents its long description.
     * 
     * @param token
     * @return
     */
    public static String longDesc(final Class<? extends ISecurityToken> token) {
        return securityTokenPart(token, TokenPart.DESC);
    }
    
    /**
     * Determines whether security token is a top level one.
     * 
     * @param token
     * @return
     */
    public static boolean isTopLevel(final Class<? extends ISecurityToken> token) {
        if (token == null) {
            throw failure(ERR_MISSING_TOKEN);
        } else if (!isWellformed(token)) {
            throw failuref(ERR_MELFORMED_TOKEN, token.getName());
        }

        return CollectionUtil.setOf(token.getInterfaces()).contains(ISecurityToken.class);
    }

    /**
     * Determines super token for the provided one. The simplest way to obtain a super token is to directly invoke token.getSuperclass(). However, this method provides an added
     * value by returning ISecurityToken class if the top level token is passed into the method. This is convenient when traversing token classes to determine when the top level is
     * reached (alternatively method isTopLevel can be used).
     * 
     * @param token
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends ISecurityToken> superToken(final Class<? extends ISecurityToken> token) {
        if (token == null) {
            throw failure(ERR_MISSING_TOKEN);
        } else if (!isWellformed(token)) {
            throw failuref(ERR_MELFORMED_TOKEN, token.getName());
        }

        return isTopLevel(token) ? ISecurityToken.class : (Class<? extends ISecurityToken>) token.getSuperclass();
    }

    /**
     * Checks whether <code>superToken</code> is a super token if <code>token</code>.
     * 
     * @param supertToken
     * @param token
     * @return
     */
    public static boolean isSuperTokenOf(final Class<? extends ISecurityToken> supertToken, final Class<? extends ISecurityToken> token) {
        return superToken(token).equals(supertToken);
    }

    /**
     * Describes possible token parts such as TITLE and DESC static fields. This is more an implementation details. 
     *
     */
    private enum TokenPart {
        TITLE, DESC;
    }

    private static String securityTokenPart(final Class<? extends ISecurityToken> token, final TokenPart part) {
        if (token == null) {
            throw failure(ERR_MISSING_TOKEN);
        }

        final Optional<Field> opTitleField = Finder.findFieldByNameOptionally(token, part.name());
        if (opTitleField.isPresent()) {
            return Try(() -> (String) opTitleField.get().get(null)).getOrElse(() -> format("Error accessing field %s for token %s.", part.name(), token.getName()));
        } else if (token.isAnnotationPresent(KeyTitle.class)) {
            return part == TokenPart.TITLE ? token.getAnnotation(KeyTitle.class).value() : token.getAnnotation(KeyTitle.class).desc();
        } else {
            throw failuref(ERR_MELFORMED_TOKEN, token.getName());
        }

    }

    /**
     * A predicate that determines if {@code token} is a well formed security token.
     *
     * @param token
     * @return
     */
    private static boolean isWellformed(final Class<? extends ISecurityToken> token) {
        return (Finder.findFieldByNameOptionally(token, TokenPart.TITLE.name()).isPresent() &&
                Finder.findFieldByNameOptionally(token, TokenPart.DESC.name()).isPresent()) ||
               token.isAnnotationPresent(KeyTitle.class);
    }

}
