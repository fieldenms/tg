package ua.com.fielden.platform.web_api;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.visibility.GraphqlFieldVisibility;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteriaUtils.isPropertyAuthorised;
import static ua.com.fielden.platform.security.tokens.Template.READ_MODEL;
import static ua.com.fielden.platform.security.tokens.TokenUtils.authoriseReading;

/// Default [GraphqlFieldVisibility] for GraphQL Web API implementation.
///
/// It governs dynamic field visibility for TG domain types on different (root and nested) levels of fields using [#READ_MODEL] tokens.
///
/// This is the single statement of the authorisation rules that decide what a user may see of the domain.
///
public class FieldVisibility implements GraphqlFieldVisibility {
    private final IAuthorisationModel authorisationModel;
    private final Map<String, Class<? extends AbstractEntity<?>>> domainTypes;
    private final ISecurityTokenProvider securityTokenProvider;

    /// Creates an instance to be installed in the code registry of a GraphQL schema.
    ///
    /// @param authorisationModel    authorises Web API queries [FieldVisibility].
    /// @param domainTypes           a set of TG domain types to be processed for field visibility
    /// @param securityTokenProvider a security token provider, used to get token classes by their string names.
    ///
    public FieldVisibility(final IAuthorisationModel authorisationModel, final Set<Class<? extends AbstractEntity<?>>> domainTypes, final ISecurityTokenProvider securityTokenProvider) {
        this.authorisationModel = authorisationModel;
        this.domainTypes = domainTypes.stream().collect(toMap(Class::getSimpleName, Function.identity()));
        this.securityTokenProvider = securityTokenProvider;
    }

    @Override
    public List<GraphQLFieldDefinition> getFieldDefinitions(final GraphQLFieldsContainer fieldsContainer) {
        final String simpleName = fieldsContainer.getName();
        // Only consider containers that represent TG domain types (more specifically only those domain types that are used for Query root fields).
        if (domainTypes.containsKey(simpleName)) {
            final var isVisible = visibilityPredicate(domainTypes.get(simpleName));
            return fieldsContainer.getFieldDefinitions().stream()
                    .filter(def -> isVisible.test(def.getName()))
                    .toList();
        }
        return fieldsContainer.getFieldDefinitions();
    }

    /// Whether the current user is authorised to read the model of `entityType`.
    ///
    public static boolean isModelReadable(
            final Class<? extends AbstractEntity<?>> entityType,
            final IAuthorisationModel authorisationModel,
            final ISecurityTokenProvider securityTokenProvider)
    {
        return authoriseReading(entityType.getSimpleName(), READ_MODEL, authorisationModel, securityTokenProvider).isSuccessful();
    }

    /// Creates a predicate that, given a property of `entityType`, returns `true` if the current user is authorised to read it.
    ///
    public Predicate<CharSequence> visibilityPredicate(final Class<? extends AbstractEntity<?>> entityType) {
        return !isModelReadable(entityType, authorisationModel, securityTokenProvider)
                // At least one field must be accessible (the ID field is used for this purpose).
                // Without it, the type would not conform to the GraphQL specification, causing validation issues in the GraphiQL editor.
                ? prop -> ID.contentEquals(prop)
                // Filter out properties that the current user is not authorised to access.
                : prop -> isPropertyAuthorised(entityType, prop.toString());
    }

    @Override
    public GraphQLFieldDefinition getFieldDefinition(final GraphQLFieldsContainer fieldsContainer, final String fieldName) {
        return getFieldDefinitions(fieldsContainer).stream()
            .filter(def -> Objects.equals(fieldName, def.getName()))
            .findAny()
            .orElse(null);
    }

}
