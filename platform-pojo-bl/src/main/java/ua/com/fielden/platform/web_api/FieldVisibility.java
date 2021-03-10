package ua.com.fielden.platform.web_api;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.security.tokens.Template.READ_MODEL;
import static ua.com.fielden.platform.security.tokens.TokenUtils.authoriseReading;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLSchema;
import graphql.schema.visibility.GraphqlFieldVisibility;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * Default {@link GraphqlFieldVisibility} for GraphQL Web API implementation.
 * <p>
 * It governs dynamic field visibility for TG domain types on different (root and nested) levels of fields using {@link Template#READ_MODEL} tokens.
 *
 * @author TG Team
 */
public class FieldVisibility implements GraphqlFieldVisibility {
    private final IAuthorisationModel authorisation;
    private final Map<String, Class<? extends AbstractEntity<?>>> domainTypes;
    private final String securityTokensPackageName;
    private final ISecurityTokenProvider securityTokenProvider;

    /**
     * Creates {@link FieldVisibility} instance to be used as a singleton for the whole {@link GraphQLSchema}.
     * 
     * @param authorisation -- authorisation model for tokens processing
     * @param domainTypes -- a set of TG domain types to be processed for field visibility
     * @param securityTokensPackageName -- a place where all security tokens are located
     * @param securityTokenProvider -- security token provider, used to get token classes by their string names.
     */
    public FieldVisibility(final IAuthorisationModel authorisation, final Set<Class<? extends AbstractEntity<?>>> domainTypes, final String securityTokensPackageName, final ISecurityTokenProvider securityTokenProvider) {
        this.authorisation = authorisation;
        this.domainTypes = domainTypes.stream().collect(toMap(type -> type.getSimpleName(), type -> type));
        this.securityTokensPackageName = securityTokensPackageName;
        this.securityTokenProvider = securityTokenProvider;
    }

    @Override
    public List<GraphQLFieldDefinition> getFieldDefinitions(final GraphQLFieldsContainer fieldsContainer) {
        final String simpleName = fieldsContainer.getName();
        if (domainTypes.containsKey(simpleName)) { // only consider containers that represent TG domain types (more specifically only those domain types that are used for Query root fields)
            if (!authoriseReading(simpleName, READ_MODEL, securityTokensPackageName, authorisation, securityTokenProvider).isSuccessful()) {
                return fieldsContainer.getFieldDefinitions().stream()
                    .filter(def -> ID.equals(def.getName())) // at least one field should be accessible (ID was chosen for that purpose); otherwise the type does not conform to GraphQL spec (and validations in GraphiQL editor become broken)
                    .collect(toList());
            }
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