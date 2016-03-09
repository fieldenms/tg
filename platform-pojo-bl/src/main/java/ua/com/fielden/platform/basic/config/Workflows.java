package ua.com.fielden.platform.basic.config;

/**
 * Application development with TG assumes two workflows: development and deployment. The workflow should be specified in application.properties file
 * to be used for starting of the server application.
 * <p>
 * If {@link #development} workflow has been chosen, then client application loads only those resources that are specified in 
 * 'desktop-application-startup-resources' ('mobile-application-startup-resources' for mobile app) file and loading is performed with Polymer. 
 * However, 'tg-element-loader' component still have all resources from 'desktop-application-startup-resources' file excluded 
 * (see importedURLs variable).
 * <p>
 * If {@link #deployment} workflow has been chosen, then client application loads vulcanized version of 'desktop-application-startup-resources' ('mobile-application-startup-resources' for mobile app) file -- 
 * 'desktop-startup-resources-vulcanized' ('mobile-startup-resources-vulcanized' for mobile app). The vulcanized version of resources includes not only those resources that are specified in 
 * 'desktop-application-startup-resources' file, but also all centres / masters that were included in IWebUiConfig configuration. 'tg-element-loader' component have all resources from 
 * 'desktop-application-startup-resources' file excluded (also centres / masters components are also excluded).
 * <p>
 * {@link #vulcanizing} workflow should not be used by application developer in application.properties file. It is used strictly inside vulcanizing utility and is internal mode.
 * 
 * @author TG Team
 * 
 */
public enum Workflows {
    /**
     * If {@link #development} workflow has been chosen, then client application loads only those resources that are specified in 
     * 'desktop-application-startup-resources' ('mobile-application-startup-resources' for mobile app) file and loading is performed with Polymer. 
     * However, 'tg-element-loader' component still have all resources from 'desktop-application-startup-resources' file excluded 
     * (see importedURLs variable).
    */
    development,
    /**
     * If {@link #deployment} workflow has been chosen, then client application loads vulcanized version of 'desktop-application-startup-resources' ('mobile-application-startup-resources' for mobile app) file -- 
     * 'desktop-startup-resources-vulcanized' ('mobile-startup-resources-vulcanized' for mobile app). The vulcanized version of resources includes not only those resources that are specified in 
     * 'desktop-application-startup-resources' file, but also all centres / masters that were included in IWebUiConfig configuration. 'tg-element-loader' component have all resources from 
     * 'desktop-application-startup-resources' file excluded (also centres / masters components are also excluded).
     */
    deployment,
    
    /**
     * {@link #vulcanizing} workflow should not be used by application developer in application.properties file. It is used strictly inside vulcanizing utility and is internal mode.
     */
    vulcanizing;
}
