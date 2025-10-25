package ua.com.fielden.platform.web_api;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLSchema;
import graphql.schema.visibility.GraphqlFieldVisibility;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteriaUtils.isPropertyAuthorised;
import static ua.com.fielden.platform.security.tokens.Template.READ_MODEL;
import static ua.com.fielden.platform.security.tokens.TokenUtils.authoriseReading;

/// Default [GraphqlFieldVisibility] for GraphQL Web API implementation.
///
/// It governs dynamic field visibility for TG domain types on different (root and nested) levels of fields using [#READ_MODEL] tokens.
///
public class FieldVisibility implements GraphqlFieldVisibility {
    private final IAuthorisationModel authorisationModel;
    private final Map<String, Class<? extends AbstractEntity<?>>> domainTypes;
    private final ISecurityTokenProvider securityTokenProvider;

    /// Creates [FieldVisibility] instance to be used as a singleton for the whole [GraphQLSchema].
    ///
    /// @param authorisationModel    authorises Web API queries [FieldVisibility].
    /// @param domainTypes           a set of TG domain types to be processed for field visibility
    /// @param securityTokenProvider a security token provider, used to get token classes by their string names.
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
            final var rootAuthorised = authoriseReading(simpleName, READ_MODEL, authorisationModel, securityTokenProvider).isSuccessful();
            return fieldsContainer.getFieldDefinitions().stream()
                .filter(def -> !rootAuthorised
                    // At least one field should be accessible (ID was chosen for that purpose).
                    // Otherwise, the type does not conform to GraphQL spec (and validations in GraphiQL editor become broken).
                    ? ID.equals(def.getName())
                    // Filter out unauthorised properties.
                    : isPropertyAuthorised(domainTypes.get(simpleName), def.getName())
                )
                .collect(toList());
        }
        return fieldsContainer.getFieldDefinitions();
    }

    @Override
    public GraphQLFieldDefinition getFieldDefinition(final GraphQLFieldsContainer fieldsContainer, final String fieldName) {
        return getFieldDefinitions(fieldsContainer).stream()
            .filter(def -> Objects.equals(fieldName, def.getName()))
            .findAny()
            .orElse(null);
    }

}
