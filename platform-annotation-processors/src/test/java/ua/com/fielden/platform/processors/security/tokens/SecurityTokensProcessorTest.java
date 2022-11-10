package ua.com.fielden.platform.processors.security.tokens;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.processors.test_utils.Compilation.OPTION_PROC_ONLY;
import static ua.com.fielden.platform.utils.MiscUtilities.substringFrom;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import org.junit.Test;

import ua.com.fielden.platform.processors.test_utils.Compilation;
import ua.com.fielden.platform.processors.test_utils.JavaFileObjectFinder;
import ua.com.fielden.platform.processors.test_utils.PrintingDiagnosticCollector;
import ua.com.fielden.platform.security.provider.Lower1LevelSecurityToken;
import ua.com.fielden.platform.security.provider.Lower2LevelSecurityToken;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.provider.Top1LevelSecurityToken;
import ua.com.fielden.platform.security.provider.Top2LevelSecurityToken;

public class SecurityTokensProcessorTest {

    @Test
    public void security_tokens_tree_is_built_correctly_with_each_level_sorted_by_token_name() throws Throwable {
        final String sourceDir = "../platform-pojo-bl/src/test/java/";
        final String pkgName = "ua.com.fielden.platform.security.provider";
        final List<JavaFileObject> sources = JavaFileObjectFinder.findSources(Path.of(sourceDir).toAbsolutePath(), pkgName);
        // hold onto processor here to use it later
        final SecurityTokensProcessor processor = new SecurityTokensProcessor();

        new Compilation(sources)
                .setProcessor(processor)
                .setDiagnosticListener(new PrintingDiagnosticCollector<>())
                .setOptions(OPTION_PROC_ONLY)
                .compileAndEvaluate(procEnv -> {
                    final SecurityTokenFinder tokenFinder = new SecurityTokenFinder(procEnv.getElementUtils(), procEnv.getTypeUtils());
                    final Set<TypeElement> tokens = processor.collectTokens(
                            // here we manually find our java sources in the processing environment to collect only security tokens from them
                            sources.stream()
                                .map(jfo -> substringBefore(substringFrom(jfo.toUri().getPath(), pkgName.replace('.', '/')), ".java")
                                        .replace('/', '.'))
                                .map(name -> tokenFinder.getTypeElement(name)) // throws a runtime exception if not found
                                .collect(toSet()));
                    //                                                        this is the method under test
                    final List<SecurityTokenNode> topTokens = new ArrayList<>(processor.buildTokenNodes(tokens));
                    assertEquals("Incorrect number of top-level tokens.", 2, topTokens.size());
                    final SecurityTokenNode t1 = topTokens.get(0);
                    assertEquals("Incorrect 1st top-level token node.", Top1LevelSecurityToken.class.getName(), t1.getToken());
                    final SecurityTokenNode t2 = topTokens.get(1);
                    assertEquals("Incorrect 2nd top-level token node.", Top2LevelSecurityToken.class.getName(), t2.getToken());

                    assertEquals("Incorrect number of subtokens for 1st top-level token.", 2, t1.getSubTokenNodes().size());
                    final List<SecurityTokenNode> t1Subtokens = new ArrayList<>(t1.getSubTokenNodes());
                    assertEquals("Incorrect 1st subtoken of 1st top-level token",
                            Lower1LevelSecurityToken.class.getName(), t1Subtokens.get(0).getToken());
                    assertEquals("Incorrect 2nd subtoken of 1st top-level token",
                            Lower2LevelSecurityToken.class.getName(), t1Subtokens.get(1).getToken());

                    assertEquals("Incorrect number of subtokens for 2nd top-level token.", 0, t2.getSubTokenNodes().size());
                });
    }

}
