package ua.com.fielden.platform.security.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.junit.Test;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.security.tokens.open_simple_master.UserMaster_CanOpen_Token;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.security.tokens.open_compound_master.OpenUserMasterAction_CanOpen_Token;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.longDesc;
import static ua.com.fielden.platform.security.SecurityTokenInfoUtils.shortDesc;
import static ua.com.fielden.platform.security.provider.ISecurityTokenProvider.ERR_DUPLICATE_SECURITY_TOKENS;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

/// A test case to ensure correct configuration of security tokens as provided for security tooling (the tree structure).
///
public class SecurityTokenProviderAndNodeConstructionTest {

    private final Injector injector = new ApplicationInjectorFactory()
            .add(new CommonEntityTestIocModuleWithPropertyFactory())
            .add(new AbstractModule() {
                @Override protected void configure() {
                    bindConstant().annotatedWith(Names.named("tokens.path")).to("target/test-classes");
                    bindConstant().annotatedWith(Names.named("tokens.package")).to("ua.com.fielden.platform.security.provider");
                }
            })
            .getInjector();

    @Test
    public void the_default_type_hierarchy_based_association_between_tokens_does_not_have_to_be_followed() {
        final SecurityTokenNode superNode = new SecurityTokenNode(Top1LevelSecurityToken.class);
        assertEquals("Incorrect token.", Top1LevelSecurityToken.class, superNode.getToken());
        assertEquals("Incorrect short desc.", shortDesc(Top1LevelSecurityToken.class), superNode.getShortDesc());
        assertEquals("Incorrect long desc.", longDesc(Top1LevelSecurityToken.class), superNode.getLongDesc());
        assertNull("Incorrect super node.", superNode.getSuperTokenNode());
        assertEquals("Incorrect number of sub tokens", 0, superNode.getSubTokenNodes().size());

        try {
            new SecurityTokenNode(Lower1LevelSecurityToken.class);
            fail("A sub token node at incorrect state was allowed to be created.");
        } catch (final Exception e) {
        }

        final SecurityTokenNode subNode = new SecurityTokenNode(Lower1LevelSecurityToken.class, superNode);

        final SecurityTokenNode subSubNode = new SecurityTokenNode(Top1LevelSecurityToken.class, subNode);

        assertEquals("Incorrect token.", Lower1LevelSecurityToken.class, subNode.getToken());
        assertEquals("Incorrect short desc.", shortDesc(Lower1LevelSecurityToken.class), subNode.getShortDesc());
        assertEquals("Incorrect long desc.", longDesc(Lower1LevelSecurityToken.class), subNode.getLongDesc());
        assertEquals("Incorrect super node.", superNode, subNode.getSuperTokenNode());
        assertEquals("Incorrect number of sub tokens", 1, subNode.getSubTokenNodes().size());
        assertEquals("Incorrect number of sub tokens", 1, superNode.getSubTokenNodes().size());
        assertEquals("Incorrect super node.", subNode, subSubNode.getSuperTokenNode());
    }

    @Test
    public void security_token_hierarchy_is_determined_correctly_for_the_specified_path_and_package() {
        final var provider = injector.getInstance(ISecurityTokenProvider.class);
        final SortedSet<SecurityTokenNode> topNodes = provider.getTopLevelSecurityTokenNodes();
        assertEquals("Incorrect number of top security tokens.", 38, topNodes.size());

        // skip attachment related security tokens before getting iterator nodesWithSkippedAttachmentTokens
        final Iterator<SecurityTokenNode> superIter = topNodes.stream().skip(19).collect(toList()).iterator();

        final SecurityTokenNode top1 = superIter.next();
        assertEquals("Incorrect first top token.", Top1LevelSecurityToken.class, top1.getToken());
        assertEquals("Incorrect number of sub tokens.", 2, top1.getSubTokenNodes().size());
        final Iterator<SecurityTokenNode> subIter = top1.getSubTokenNodes().iterator();
        assertEquals("Incorrect sub token.", Lower1LevelSecurityToken.class, subIter.next().getToken());
        assertEquals("Incorrect sub token.", Lower2LevelSecurityToken.class, subIter.next().getToken());

        final SecurityTokenNode top2 = superIter.next();
        assertEquals("Incorrect second top token.", Top2LevelSecurityToken.class, top2.getToken());
        assertEquals("Incorrect number of sub tokens.", 0, top2.getSubTokenNodes().size());
    }

    @Test
    public void security_token_hierarchy_cannot_be_build_if_tokens_have_duplicate_simple_class_name() {
        final var localInjector = new ApplicationInjectorFactory()
                .add(new CommonEntityTestIocModuleWithPropertyFactory())
                .add(new AbstractModule() {
                    @Override protected void configure() {
                        bindConstant().annotatedWith(Names.named("tokens.path")).to("target/test-classes");
                        // Use a package that contains duplicate tokens
                        bindConstant().annotatedWith(Names.named("tokens.package")).to("ua.com.fielden.platform.security");
                    }
                })
                .getInjector();

        try {
            localInjector.getInstance(ISecurityTokenProvider.class);
            fail("Security token provider did not detect duplicate tokens.");
        } catch (final ProvisionException ex) {
            assertEquals(ERR_DUPLICATE_SECURITY_TOKENS, ex.getCause().getMessage());
        }
    }

    @Test
    public void security_token_provider_can_be_used_to_exclude_standard_tokens() {
        final var standardProvider = injector.getInstance(ISecurityTokenProvider.class);
        assertTrue(standardProvider.getTokenByName(SecurityTokenProviderWithExclusion.EXCLUDED_TOKEN.getSimpleName()).isPresent());

        final var providerWithExclusion = injector.getInstance(SecurityTokenProviderWithExclusion.class);
        assertFalse(providerWithExclusion.getTokenByName(SecurityTokenProviderWithExclusion.EXCLUDED_TOKEN.getSimpleName()).isPresent());
    }

    private static class SecurityTokenProviderWithExclusion extends SecurityTokenProvider {
        static final Class<UserMaster_CanOpen_Token> EXCLUDED_TOKEN = UserMaster_CanOpen_Token.class;

        @Inject
        SecurityTokenProviderWithExclusion(final @Named("tokens.path") String path, final @Named("tokens.package") String packageName) {
            super(path, packageName, setOf(), setOf(EXCLUDED_TOKEN));
        }
    }

    @Test
    public void security_token_provider_can_be_used_to_include_extra_tokens() {
        final var standardProvider = injector.getInstance(ISecurityTokenProvider.class);
        assertFalse(standardProvider.getTokenByName(SecurityTokenProviderWithExtra.EXTRA_TOKEN.getSimpleName()).isPresent());

        final var providerWithExclusion = injector.getInstance(SecurityTokenProviderWithExtra.class);
        assertTrue(providerWithExclusion.getTokenByName(SecurityTokenProviderWithExtra.EXTRA_TOKEN.getSimpleName()).isPresent());
    }

    private static class SecurityTokenProviderWithExtra extends SecurityTokenProvider {
        static final Class<OpenUserMasterAction_CanOpen_Token> EXTRA_TOKEN = OpenUserMasterAction_CanOpen_Token.class;

        @Inject
        SecurityTokenProviderWithExtra(final @Named("tokens.path") String path, final @Named("tokens.package") String packageName) {
            super(path, packageName, Set.of(EXTRA_TOKEN), Set.of());
        }
    }

}
