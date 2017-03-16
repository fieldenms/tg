package ua.com.fielden.platform.web.test.server;

import static graphql.Scalars.*;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.util.Series;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * Web UI Testing Server launching class for full web server with platform Web UI web application and domain-driven persistent storage.
 *
 * @author TG Team
 *
 */
public class StartSecure {
    private static final Logger LOGGER = Logger.getLogger(StartSecure.class);

    public static void main(final String[] args) throws IOException {
        final String fileName = "src/main/resources/application.properties";
        final Properties props = new Properties();
        try (final InputStream st = new FileInputStream(fileName);) {
            props.load(st);
        }

        DOMConfigurator.configure(props.getProperty("log4j"));

        LOGGER.info("Starting...");
        final TgTestApplicationConfiguration component = new TgTestApplicationConfiguration(props);
        //component.getServers().add(Protocol.HTTP, Integer.parseInt(props.getProperty("port")));

        final org.restlet.Server server = component.getServers().add(Protocol.HTTPS, Integer.parseInt(props.getProperty("port")));
        final Series<Parameter> parameters = server.getContext().getParameters();

        parameters.add("sslContextFactory", "org.restlet.engine.ssl.DefaultSslContextFactory");
        // I have created self signed certificate. reference is attached with parameter
        parameters.add("keystorePath", ResourceLoader.getURL("tls/ca-signed-keystore").getPath());

        parameters.add("keystorePassword", "changeit");
        parameters.add("keyPassword", "changeit");
        parameters.add("keystoreType", "JKS");

        try {
            component.start();
            LOGGER.info("started");
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(100);
        }
        
//        final GraphQLObjectType queryType = newObject()
//                .name("helloWorldQuery")
//                .field(newFieldDefinition()
//                        .type(GraphQLString)
//                        .name("hello")
//                        .staticValue("world"))
//                .build();
        
        final ICompanionObjectFinder coFinder = component.injector().getInstance(ICompanionObjectFinder.class);
        final GraphQLObjectType queryType = newObject()
                .name("BasicQuery")
                .field(newFieldDefinition()
                        .name("tgVehicle")
                        .type(new GraphQLList(new GraphQLTypeReference("TgVehicle")))
                        .dataFetcher(new RootEntityDataFetcher(TgVehicle.class, coFinder)))
                        // .staticValue("world"))
                .build();
        
        final GraphQLSchema schema = GraphQLSchema.newSchema()
                .query(queryType)
                .build(createDictionary());
                // .build();

        final GraphQL graphQL = new GraphQL(schema); // GraphQL.newGraphQL(schema).build();

        final ExecutionResult execResult = graphQL.execute(
            ""
            + "{\n"
            + "  allVehicles: tgVehicle {\n"
            + "    key\n"
            + "    active\n"
            + "    model {\n"
            + "      key\n"
            + "      desc\n"
            + "      make {\n"
            + "        key\n"
            + "      }\n"
            + "    }\n"
            + "    replacedBy {\n"
            + "      key\n"
            + "    }\n"
            + "  }\n"
            + "  anotherAllVehicles: tgVehicle {\n"
            + "    key\n"
            + "    active\n"
            + "  }\n"
            + "}"
        );
        if (!execResult.getErrors().isEmpty()) {
            LOGGER.error("============ GraphQL Errors: ============");
            for (final GraphQLError error: execResult.getErrors()) {
                LOGGER.error(String.format("message [%s] errorType [%s] locations [%s]", error.getMessage(), error.getErrorType(), error.getLocations()));
            }
        }
        LOGGER.error("============ GraphQL Data: ============");
        final Map<String, Object> data = (Map<String, Object>) execResult.getData(); // graphQL.execute("{hello}").getData();
        LOGGER.error(data);
    }
    
    private static class RootEntityDataFetcher implements DataFetcher {
        private final Class<? extends AbstractEntity> entityType;
        private final ICompanionObjectFinder coFinder;
        
        public RootEntityDataFetcher(final Class<? extends AbstractEntity> entityType, final ICompanionObjectFinder coFinder) {
            this.entityType = entityType;
            this.coFinder = coFinder;
        }
        
        @Override
        public Object get(final DataFetchingEnvironment environment) {
//            final Object source = environment.getSource();
//            if (source == null) {
//                return null;
//            }
//            if (source instanceof Map) {
//                return ((Map<?, ?>) source).get(propertyName);
//            }
            try {
                final EntityResultQueryModel eqlQuery = select(entityType).model();
                final IEntityDao<? extends AbstractEntity> co = coFinder.find(entityType);
                final List entities = co.getAllEntities(from(eqlQuery).with(fetchAll(entityType)).model()); // TODO fetch order etc.
                return entities;
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            return null;
        }
    }
    
    private static Set<GraphQLType> createDictionary() {
        final Set<GraphQLType> types = new LinkedHashSet<>();
        
        final GraphQLObjectType tgVehicleObjectType = newObject()
                .name("TgVehicle")
                .description("Vehicle in TG example app")
                .field(newFieldDefinition()
                        .name("key")
                        .description("Vehicle Key")
                        .type(GraphQLString))
                .field(newFieldDefinition()
                        .name("model")
                        .description("Vehicle Model")
                        .type(new GraphQLTypeReference("TgVehicleModel")))
                .field(newFieldDefinition()
                        .name("active")
                        .description("Active")
                        .type(GraphQLBoolean))
                .field(newFieldDefinition()
                        .name("replacedBy")
                        .description("Replaced By")
                        .type(new GraphQLTypeReference("TgVehicle")))
                .build();
        types.add(tgVehicleObjectType);
        
        final GraphQLObjectType tgVehicleModelObjectType = newObject()
                .name("TgVehicleModel")
                .description("Vehicle Model in TG example app")
                .field(newFieldDefinition()
                        .name("key")
                        .description("Vehicle Model Key")
                        .type(GraphQLString))
                .field(newFieldDefinition()
                        .name("desc")
                        .description("Vehicle Model Desc")
                        .type(GraphQLString))
                .field(newFieldDefinition()
                        .name("make")
                        .description("Vehicle Make")
                        .type(new GraphQLTypeReference("TgVehicleMake")))
                .build();
        types.add(tgVehicleModelObjectType);
        
        final GraphQLObjectType tgVehicleMakeObjectType = newObject()
                .name("TgVehicleMake")
                .description("Vehicle Make in TG example app")
                .field(newFieldDefinition()
                        .name("key")
                        .description("Vehicle Make Key")
                        .type(GraphQLString))
                .build();
        types.add(tgVehicleMakeObjectType);
        
        return types;
    }
}