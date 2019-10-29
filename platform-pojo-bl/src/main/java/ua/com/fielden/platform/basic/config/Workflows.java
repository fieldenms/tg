package ua.com.fielden.platform.basic.config;

/**
 * Application development with TG assumes two workflows: development and deployment. The workflow should be specified in application.properties file
 * to be used for starting of the server application.
 * <p>
 * If {@link #development} workflow has been chosen, then client application loads only those resources that are specified in 
 * 'application-startup-resources' file and loading is performed with Polymer. 
 * <p>
 * If {@link #deployment} workflow has been chosen, then client application loads vulcanized version of 'application-startup-resources' file -- 
 * 'startup-resources-vulcanized'. The vulcanized version of resources includes not only those resources that are specified in 
 * 'application-startup-resources' file, but also all centres / masters that were included in IWebUiConfig configuration.
 * <p>
 * {@link #vulcanizing} workflow should not be used by application developer in application.properties file. It is used strictly inside vulcanizing utility and is internal mode.
 * 
 * @author TG Team
 * 
 */
public enum Workflows {
    /**
     * If {@link #development} workflow has been chosen, then client application loads only those resources that are specified in 
     * 'application-startup-resources' file and loading is performed with Polymer. 
     */
    development,
    /**
     * If {@link #deployment} workflow has been chosen, then client application loads vulcanized version of 'application-startup-resources' file -- 
     * 'startup-resources-vulcanized'. The vulcanized version of resources includes not only those resources that are specified in 
     * 'application-startup-resources' file, but also all centres / masters that were included in IWebUiConfig configuration.
     */
    deployment,
    
    /**
     * {@link #vulcanizing} workflow should not be used by application developer in application.properties file. It is used strictly inside vulcanizing utility and is internal mode.
     */
    vulcanizing;
}