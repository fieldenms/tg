package ua.com.fielden.platform.basic.config;

/**
 * Application development with TG assumes two workflows: development and deplyment.
 * These workflows influence the way main menu gets created and initialised.
 * Specifically, in case of the development workflow the main menu is constructed from the Java configuration class and initialised from the configured in application.properties directory location.
 * In case of the deployment workflow, the main menu is constructed and initialised form the cloud.
 *
 * @author TG Team
 *
 */
public enum Workflows {
    development, deployment;

    public static void main(final String[] args) {
	System.out.println(Workflows.development.equals(Workflows.valueOf("development")));
    }
}
