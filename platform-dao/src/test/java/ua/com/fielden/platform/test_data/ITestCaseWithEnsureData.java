package ua.com.fielden.platform.test_data;

import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.DbCreator;

/**
 * A contract that should be implemented by a base test case, usually named {@code AbstractDomainTestCase}.
 * It declared method, required by {@link EnsureDdataInterceptor} to processes domain-specific test cases.
 *
 * @author TG Team
 *
 */
public interface ITestCaseWithEnsureData {

    /**
     * Provides a way to set up a clean up routine that should be executed after the initial pre-population of all the test data.
     * {@link EnsureDataInterceptor} takes care of assigning the necessary cleaning routine. 
     *
     * @param clean
     */
    void setCleanUpAfterPrepopulation(final Runnable clean);

    /**
     * A way for {@link EnsureDataInterceptor} to get access to {@link DbCreator}.
     * It is implemented in {@link AbstractDomainDrivenTestCase}.
     *
     * @return
     */
    DbCreator getDbCreator();
    
    /**
     * Returns {@code true} if test data cashing is not in play. Skipping caching informs {@link EnsureDataInterceptor} to executed the data population and create the relevant SQL scripts that can be reused subsequently.
     * <p>
     * Caching has a dynamic nature and may depend on the way tests are executed.
     * For example, if a test case is executed manually from an IDE, caching should be skipped to correctly populate the data.
     * But subsequent test case executions that do not need to change the test data, would benefit from not skipping the cache, which result in much faster data population.
     * It is often the case that this method is implemented at an application level as:
     * <pre>
     *  public boolean skipCaching() {
     *      return saveDataPopulationScriptToFile() || useSavedDataPopulationScript();
     *  }
     * </pre>
     *
     * @return
     */
    boolean skipCaching();
    
    /**
     * A way for {@link EnsureDataInterceptor} to access the current transaction GUID.
     * It is implemented in {@link AbstractDomainDrivenTestCase}.
     *
     * @return
     */
    String getTransactionGuid();

}