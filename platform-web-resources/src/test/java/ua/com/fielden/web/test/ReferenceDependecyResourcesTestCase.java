package ua.com.fielden.web.test;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;
import org.restlet.Restlet;
import org.restlet.Router;

import ua.com.fielden.platform.test.DbDrivenTestCase2;
import ua.com.fielden.platform.update.EChecksumMismatch;
import ua.com.fielden.platform.update.IDownloadProgress;
import ua.com.fielden.platform.update.IReferenceDependancyController;
import ua.com.fielden.platform.update.ReferenceDependancyController;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.ReferenceDependencyDownloadResourceFactory;
import ua.com.fielden.platform.web.ReferenceDependencyListResourceFactory;
import ua.com.fielden.platform.web.resources.ReferenceDependencyDownloadResource;
import ua.com.fielden.platform.web.resources.ReferenceDependencyListResource;
import ua.com.fielden.platform.web.test.WebBasedTestCase;

/**
 * Provides unit tests for {@link ReferenceDependencyDownloadResource} and {@link ReferenceDependencyListResource} web resources and {@link ReferenceDependancyController}
 * controller.
 *
 * @author TG Team
 *
 */
public class ReferenceDependecyResourcesTestCase extends WebBasedTestCase {
    private final static String REFERENCE_DEPENDENCIES_LOCATION = "src/test/resources/data-files/dependencies";

    private final static String DEPENDENCY_01_NAME = "dependency-01.txt";
    private final static String DEPENDENCY_01_CHECKSUM = "C5A64A96A7784B933B14EF5749AD6E3918EEA13F";
    private final static String DEPENDENCY_01_INCORRECT_CHECKSUM = "B5A64A96A7784B933B14EF5749AD6E3918EEA13F";
    private final static Long DEPENDENCY_01_SIZE = 20L;

    private final static String DEPENDENCY_02_NAME = "dependency-02.txt";
    private final static String DEPENDENCY_02_CHECKSUM = "3EF5F096159119B2771DA4C0588160A89480740B";
    private final static Long DEPENDENCY_02_SIZE = 21L;

    private final IReferenceDependancyController controller = new ReferenceDependancyController(config.restClientUtil());

    @Override
    public synchronized Restlet getRoot() {
	final Router router = new Router(getContext());
	router.attach("/users/{username}/dependencies/{file-name}", new ReferenceDependencyDownloadResourceFactory(REFERENCE_DEPENDENCIES_LOCATION, DbDrivenTestCase2.injector));
	router.attach("/users/{username}/update", new ReferenceDependencyListResourceFactory(REFERENCE_DEPENDENCIES_LOCATION, DbDrivenTestCase2.injector));
	return router;
    }

    @Test
    public void test_dependency_info_retrieval() {
	final Map<String, Pair<String, Long>> map = controller.dependencyInfo();

	assertEquals("Incorrect number of dependencies.", 2, map.size());

	assertEquals("Incorrect dependency size.", DEPENDENCY_01_SIZE, map.get(DEPENDENCY_01_NAME).getValue());
	assertEquals("Incorrect dependency checksum.", DEPENDENCY_01_CHECKSUM, map.get(DEPENDENCY_01_NAME).getKey());

	assertEquals("Incorrect dependency size.", DEPENDENCY_02_SIZE, map.get(DEPENDENCY_02_NAME).getValue());
	assertEquals("Incorrect dependency checksum.", DEPENDENCY_02_CHECKSUM, map.get(DEPENDENCY_02_NAME).getKey());
    }

    @Test
    public void test_dependency_download() {
	final DependencyProgress dp = new DependencyProgress();
	final byte[] dependency_content = controller.download(DEPENDENCY_01_NAME, DEPENDENCY_01_CHECKSUM, dp);
	assertEquals("Incorrect dependency content.", DEPENDENCY_01_SIZE, Long.valueOf(dependency_content.length));
	assertEquals("Incorrect update info for dependency.", DEPENDENCY_01_SIZE, dp.progress);
    }

    @Test(expected = EChecksumMismatch.class)
    public void test_dependency_download_checksum_error_handling() {
	final DependencyProgress dp = new DependencyProgress();
	controller.download(DEPENDENCY_01_NAME, DEPENDENCY_01_INCORRECT_CHECKSUM, dp);
    }

    @Override
    protected String[] getDataSetPaths() {
	return new String[] {};
    }

    private static final class DependencyProgress implements IDownloadProgress {
	private Long progress = 0L;

	@Override
	public void update(final long incNumOfBytesRead) {
	    progress = incNumOfBytesRead;
	}

    }

}
