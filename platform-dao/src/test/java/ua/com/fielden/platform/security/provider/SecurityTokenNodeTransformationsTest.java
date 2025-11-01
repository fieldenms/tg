package ua.com.fielden.platform.security.provider;

import org.junit.Test;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.security.ISecurityToken;

import java.util.Set;
import java.util.SortedSet;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.security.provider.SecurityTokenNodeTransformations.ERR_TOKEN_NOT_IN_TREE;

public class SecurityTokenNodeTransformationsTest {


    interface In {
        class A1 implements ISecurityToken { public static final String TITLE = "A1", DESC = "A1"; }
        class A2_1 extends A1 { public static final String TITLE = "A2_1", DESC = "A2_1"; }
        class A3_1 extends A2_1 { public static final String TITLE = "A3_1", DESC = "A3_1"; }
        class A2_2 extends A1 { public static final String TITLE = "A2_2", DESC = "A2_2"; }

        class B1 implements ISecurityToken { public static final String TITLE = "B1", DESC = "B1";}
        class B2_1 extends B1 {  public static final String TITLE = "B2_1", DESC = "B2_1"; }
        class B3_1 extends B2_1 {  public static final String TITLE = "B3_1", DESC = "B3_1"; }
    }

    @Test
    public void parent_of_top_level_node_can_be_changed() {
        interface Expected {
            class B1 implements ISecurityToken { public static final String TITLE = "B1", DESC = "B1";}
            class A1 extends B1 { public static final String TITLE = "A1", DESC = "A1";}
            class A2_1 extends A1 { public static final String TITLE = "A2_1", DESC = "A2_1"; }
            class A3_1 extends A2_1 { public static final String TITLE = "A3_1", DESC = "A3_1"; }
            class A2_2 extends A1 { public static final String TITLE = "A2_2", DESC = "A2_2"; }
            class B2_1 extends B1 {  public static final String TITLE = "B2_1", DESC = "B2_1"; }
            class B3_1 extends B2_1 {  public static final String TITLE = "B3_1", DESC = "B3_1"; }
        }

        final var tree = mkTokenTree(In.class);
        final var expected = mkTokenTree(Expected.class);
        final var transformed = SecurityTokenNodeTransformations.setParentOf(In.A1.class, In.B1.class).transform(tree);
        assertTreeEquals(expected, transformed);
    }

    @Test
    public void parent_of_intermediate_node_can_be_changed() {
        interface Expected {
            class A1 implements ISecurityToken { public static final String TITLE = "A1", DESC = "A1"; }
            class A2_2 extends A1 { public static final String TITLE = "A2_2", DESC = "A2_2"; }

            class B1 implements ISecurityToken { public static final String TITLE = "B1", DESC = "B1";}
            class B2_1 extends B1 {  public static final String TITLE = "B2_1", DESC = "B2_1"; }
            class A2_1 extends B2_1 { public static final String TITLE = "A2_1", DESC = "A2_1"; }
            class A3_1 extends A2_1 { public static final String TITLE = "A3_1", DESC = "A3_1"; }
            class B3_1 extends B2_1 {  public static final String TITLE = "B3_1", DESC = "B3_1"; }
        }

        final var tree = mkTokenTree(In.class);
        final var expected = mkTokenTree(Expected.class);
        final var transformed = SecurityTokenNodeTransformations.setParentOf(In.A2_1.class, In.B2_1.class).transform(tree);
        assertTreeEquals(expected, transformed);
    }

    @Test
    public void parent_of_leaf_node_can_be_changed() {
        interface Expected {
            class A1 implements ISecurityToken { public static final String TITLE = "A1", DESC = "A1"; }
            class A2_1 extends A1 { public static final String TITLE = "A2_1", DESC = "A2_1"; }
            class A3_1 extends A2_1 { public static final String TITLE = "A3_1", DESC = "A3_1"; }

            class B1 implements ISecurityToken { public static final String TITLE = "B1", DESC = "B1";}
            class B2_1 extends B1 {  public static final String TITLE = "B2_1", DESC = "B2_1"; }
            class B3_1 extends B2_1 {  public static final String TITLE = "B3_1", DESC = "B3_1"; }
            class A2_2 extends B1 { public static final String TITLE = "A2_2", DESC = "A2_2"; }
        }

        final var tree = mkTokenTree(In.class);
        final var expected = mkTokenTree(Expected.class);
        final var transformed = SecurityTokenNodeTransformations.setParentOf(In.A2_2.class, In.B1.class).transform(tree);
        assertTreeEquals(expected, transformed);
    }

    @Test
    public void specified_parent_must_be_present_in_the_tree() {
        class AuxToken implements ISecurityToken {}

        final var tree = mkTokenTree(In.class);
        assertThatThrownBy(() -> SecurityTokenNodeTransformations.setParentOf(In.A2_2.class, AuxToken.class).transform(tree))
                .isInstanceOf(InvalidStateException.class)
                .hasMessage(ERR_TOKEN_NOT_IN_TREE.formatted(AuxToken.class.getTypeName()));
    }

    private static SortedSet<SecurityTokenNode> mkTokenTree(final Class<?> testClass) {
        return SecurityTokenProvider.buildTokenNodes((Set) Set.of(testClass.getDeclaredClasses()));
    }

    private static boolean treeEquals(final Set<SecurityTokenNode> tree1, final Set<SecurityTokenNode> tree2) {
        return tree1.isEmpty() && tree2.isEmpty()
               || tree1.stream().map(node -> node.getToken().getSimpleName()).collect(toSet())
                          .equals(tree2.stream().map(node -> node.getToken().getSimpleName()).collect(toSet()))
                  && treeEquals(tree1.stream().flatMap(node -> node.getSubTokenNodes().stream()).collect(toSet()),
                                tree2.stream().flatMap(node -> node.getSubTokenNodes().stream()).collect(toSet()));
    }

    private static void assertTreeEquals(final Set<SecurityTokenNode> expected, final Set<SecurityTokenNode> actual) {
        assertTrue(treeEquals(expected, actual));
    }

}
