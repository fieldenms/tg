package ua.com.fielden.platform.web.test.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.util.Series;

import graphql.ExecutionResult;
import graphql.GraphQLError;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web_api.GraphQLService;
import ua.com.fielden.platform.web_api.Prettyfier;

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
        
        final GraphQLService graphQLService = component.injector().getInstance(GraphQLService.class);
        final String queryString = 
            ""
            + "{\n"
            + "  testEntities: tgPersistentEntityWithProperties {\n"
            + "    key\n"
            + "    desc\n"
            + "    booleanProp(value: true)\n"
            + "  }\n"
            + "  allVehicles: tgVehicle {\n"
            + "    key\n"
            + "    active(value: true)\n"
            + "    fuelUsages {\n"
            + "      qty\n"
            + "    }\n"
            + "    model {\n"
            + "      key\n"
            + "      desc\n"
            + "      make {\n"
            + "        key\n"
            + "      }\n"
            + "    }\n"
            + "    replacedBy {\n"
            + "      key\n"
            + "      active(value: false)\n"
            + "    }\n"
            + "  }\n"
            + "  anotherAllVehicles: tgVehicle {\n"
            + "    key\n"
            + "    active\n"
            + "  }\n"
            + "}";
        final ExecutionResult execResult = graphQLService.graphQL.execute(queryString);
        LOGGER.error("============ GraphQL Query: ============");
        LOGGER.error(queryString);
        if (!execResult.getErrors().isEmpty()) {
            LOGGER.error("============ GraphQL Errors: ============");
            for (final GraphQLError error: execResult.getErrors()) {
                LOGGER.error(String.format("message [%s] errorType [%s] locations [%s]", error.getMessage(), error.getErrorType(), error.getLocations()));
            }
        }
        LOGGER.error("============ GraphQL Data: ============");
        final Map<String, Object> data = (Map<String, Object>) execResult.getData(); // graphQL.execute("{hello}").getData();
        LOGGER.error(Prettyfier.prettyString(data, 0, true));
    }
}