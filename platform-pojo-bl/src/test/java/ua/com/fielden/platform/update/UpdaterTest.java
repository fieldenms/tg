package ua.com.fielden.platform.update;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ua.com.fielden.platform.utils.Pair;

/**
 * Test the application update logic.
 * 
 * @author TG Team
 * 
 */
public class UpdaterTest {

    private final Updater updater;

    public UpdaterTest() throws Exception {
        updater = new Updater("src/test/resources", null, null);
    }

    @Test
    public void test_algorithm_for_determining_update_actions() {
        final Map<String, Pair<String, Long>> referenceDependencies = new HashMap<String, Pair<String, Long>>();
        referenceDependencies.put("file1.jar", new Pair<String, Long>("9BA1", 23L)); // updated dependency
        referenceDependencies.put("file2.jar", new Pair<String, Long>("14FF", 1569L)); // new dependency
        referenceDependencies.put("file3.jar", new Pair<String, Long>("8EB3", 56L)); // unchanged dependency
        final Map<String, Pair<String, Long>> localDependencies = new HashMap<String, Pair<String, Long>>();
        localDependencies.put("file1.jar", new Pair<String, Long>("14FF", 456L)); // up for an update
        localDependencies.put("file3.jar", new Pair<String, Long>("8EB3", 56L)); // no changes
        localDependencies.put("file4.jar", new Pair<String, Long>("5D75", 456L)); // up for deletion
        localDependencies.put("file5.jar", new Pair<String, Long>("EC8A", 456L)); // up for deletion

        final Map<String, DependencyAction> dependencyAction = updater.determineActions(localDependencies, referenceDependencies);

        assertEquals("Incorrect number of dependencies that require action.", 4, dependencyAction.keySet().size());
        assertEquals("Incorrect action.", DependencyAction.UPDATE, dependencyAction.get("file1.jar"));
        assertEquals("Incorrect action.", DependencyAction.UPDATE, dependencyAction.get("file2.jar"));
        assertEquals("Incorrect action.", DependencyAction.DELETE, dependencyAction.get("file4.jar"));
        assertEquals("Incorrect action.", DependencyAction.DELETE, dependencyAction.get("file5.jar"));
    }

    @Test
    public void test_algorithm_for_determining_update_actions_for_a_new_app_installation() {
        final Map<String, Pair<String, Long>> referenceDependencies = new HashMap<String, Pair<String, Long>>();
        referenceDependencies.put("file1.jar", new Pair<String, Long>("9BA1", 23L)); // updated dependency
        referenceDependencies.put("file2.jar", new Pair<String, Long>("14FF", 1569L)); // new dependency
        referenceDependencies.put("file3.jar", new Pair<String, Long>("8EB3", 56L)); // unchanged dependency
        final Map<String, Pair<String, Long>> localDependencies = new HashMap<String, Pair<String, Long>>();

        final Map<String, DependencyAction> dependencyAction = updater.determineActions(localDependencies, referenceDependencies);

        assertEquals("Incorrect number of dependencies that require action.", 3, dependencyAction.keySet().size());
        assertEquals("Incorrect action.", DependencyAction.UPDATE, dependencyAction.get("file1.jar"));
        assertEquals("Incorrect action.", DependencyAction.UPDATE, dependencyAction.get("file2.jar"));
        assertEquals("Incorrect action.", DependencyAction.UPDATE, dependencyAction.get("file3.jar"));
    }
}
