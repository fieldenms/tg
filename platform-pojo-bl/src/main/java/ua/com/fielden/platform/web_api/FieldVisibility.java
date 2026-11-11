package ua.com.fielden.platform.web_api;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.visibility.GraphqlFieldVisibility;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.tokens.Template;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteriaUtils.isPropertyAuthorised;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.baseEntityType;
import static ua.com.fielden.platform.security.tokens.Template.READ_MODEL;
import static ua.com.fielden.platform.security.tokens.TokenUtils.authoriseReading;
import static ua.com.fielden.platform.utils.CollectionUtil.first;

/// Default [GraphqlFieldVisibility] for GraphQL Web API implementation.
///
/// It governs dynamic field visibility for TG domain types on different (root and nested) levels of fields using [#READ_MODEL] tokens.
///
/// This is the single statement of the authorisation rules that decide what a user may see of the domain.
///
public class FieldVisibility implements GraphqlFieldVisibility {
    private final IAuthorisationModel authorisationModel;
    private final Map<String, Class<? extends AbstractEntity<?>>> entityTypes;
    private final ISecurityTokenProvider securityTokenProvider;

    /// Creates an instance to be installed in the code registry of a GraphQL schema.
    ///
    /// @param authorisationModel    authorises Web API queries [FieldVisibility].
    /// @param entityTypes           entity types to be processed for field visibility
    /// @param securityTokenProvider a security token provider, used to get token classes by their string names.
    ///
    public FieldVisibility(final IAuthorisationModel authorisationModel, final Collection<WebApiEntityType> entityTypes, final ISecurityTokenProvider securityTokenProvider) {
        this.authorisationModel = authorisationModel;
        this.entityTypes = entityTypes.stream().collect(toMap(WebApiEntityType::name, WebApiEntityType::type));
        this.securityTokenProvider = securityTokenProvider;
    }

    @Override
    public List<GraphQLFieldDefinition> getFieldDefinitions(final GraphQLFieldsContainer fieldsContainer) {
        final String typeName = fieldsContainer.getName();
        // Only consider containers that represent entity types with root fields.
        if (entityTypes.containsKey(typeName)) {
            return filterFieldDefinitions(entityTypes.get(typeName), fieldsContainer.getFieldDefinitions());
        }
        else {
            return fieldsContainer.getFieldDefinitions();
        }
    }

    /// Whether the current user is authorised to read the model of `entityType`.
    ///
    public static boolean isModelReadable(
            final Class<? extends AbstractEntity<?>> entityType,
            final IAuthorisationModel authorisationModel,
            final ISecurityTokenProvider securityTokenProvider)
    {
        return authoriseReadingOf(entityType, READ_MODEL, authorisationModel, securityTokenProvider).isSuccessful();
    }

    /// Authorises the current user to read `entityType` as specified by `readingKind`.
    ///
    /// A generated type is authorised as its base type, which is the type that security tokens are defined for.
    ///
    public static Result authoriseReadingOf(
            final Class<? extends AbstractEntity<?>> entityType,
            final Template readingKind,
            final IAuthorisationModel authorisationModel,
            final ISecurityTokenProvider securityTokenProvider)
    {
        return authoriseReading(baseEntityType(entityType).getSimpleName(), readingKind, authorisationModel, securityTokenProvider);
    }

    /// Creates a predicate that, given a property of `entityType`, returns `true` if the current user is authorised to read it.
    ///
    public static Predicate<CharSequence> visibilityPredicate(
            final Class<? extends AbstractEntity<?>> entityType,
            final IAuthorisationModel authorisationModel,
            final ISecurityTokenProvider securityTokenProvider)
    {
        return !isModelReadable(entityType, authorisationModel, securityTokenProvider)
                // At least one field must be accessible (the ID field is used for this purpose).
                // Without it, the type would not conform to the GraphQL specification, causing validation issues in the GraphiQL editor.
                ? prop -> ID.contentEquals(prop)
                // Filter out properties that the current user is not authorised to access.
                : prop -> isPropertyAuthorised(entityType, prop.toString());
    }

    @Override
    public GraphQLFieldDefinition getFieldDefinition(final GraphQLFieldsContainer fieldsContainer, final String fieldName) {
        final String typeName = fieldsContainer.getName();
        // Only consider containers that represent entity types with root fields.
        if (entityTypes.containsKey(typeName)) {
            final var fieldDefinition = fieldsContainer.getFieldDefinition(fieldName);
            return fieldDefinition == null ? null : first(filterFieldDefinitions(entityTypes.get(typeName), List.of(fieldDefinition))).orElse(null);
        }
        else {
            return fieldsContainer.getFieldDefinition(fieldName);
        }
    }

    private List<GraphQLFieldDefinition> filterFieldDefinitions(final Class<? extends AbstractEntity<?>> entityType, final List<GraphQLFieldDefinition> fieldDefinitions) {
        final var isVisible = visibilityPredicate(entityType, authorisationModel, securityTokenProvider);
        return fieldDefinitions.stream().filter(def -> isVisible.test(def.getName())).toList();
    }

}
