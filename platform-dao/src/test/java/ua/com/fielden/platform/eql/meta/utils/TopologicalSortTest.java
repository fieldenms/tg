package ua.com.fielden.platform.eql.meta.utils;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ua.com.fielden.platform.eql.meta.utils.TopologicalSort.sortTopologically;

/// Tests for [TopologicalSort], with emphasis on how a stalled sort is diagnosed.
///
/// A stall has two causes -- a cycle, or a dangling edge -- and only the former yields
/// [TopologicalSortException#cycle()].
///
/// [TopologicalSort] copies the input into a [java.util.HashMap], so the vertex that cycle extraction starts
/// from is not controlled by the caller.
/// Assertions here are therefore invariant under the choice of starting vertex.
///
public class TopologicalSortTest {

    @Test
    public void a_well_formed_acyclic_graph_is_sorted_with_dependencies_preceding_their_dependants() throws Exception {
        // top -> mid -> bottom
        final var sorted = sortTopologically(graph("top", Set.of("mid"),
                                                  "mid", Set.of("bottom"),
                                                  "bottom", Set.of()));

        assertThat(sorted).containsExactly("bottom", "mid", "top");
    }

    @Test
    public void a_cycle_in_a_well_formed_graph_is_reported_with_its_participants() {
        // alpha -> beta -> gamma -> alpha
        final var graph = graph("alpha", Set.of("beta"),
                                "beta", Set.of("gamma"),
                                "gamma", Set.of("alpha"));

        assertThatThrownBy(() -> sortTopologically(graph))
                .isInstanceOf(TopologicalSortException.class)
                .satisfies(ex -> {
                    // The reported cycle is some rotation of [alpha, beta, gamma], closed by a repeat of its first vertex.
                    final var cycle = cycleOf(ex);
                    assertThat(cycle).hasSize(4);
                    assertThat(cycle.getFirst()).isEqualTo(cycle.getLast());
                    assertThat(cycle).containsOnly("alpha", "beta", "gamma");
                });
    }

    @Test
    public void a_vertex_that_merely_leads_into_a_cycle_is_trimmed_from_the_reported_cycle() {
        // leadIn -> a -> b -> a; `leadIn` is not part of the cycle and must not be reported as one of its participants.
        final var graph = graph("leadIn", Set.of("a"),
                                "a", Set.of("b"),
                                "b", Set.of("a"));

        assertThatThrownBy(() -> sortTopologically(graph))
                .isInstanceOf(TopologicalSortException.class)
                .satisfies(ex -> {
                    final var cycle = cycleOf(ex);
                    assertThat(cycle).doesNotContain("leadIn");
                    assertThat(cycle).hasSize(3);
                    assertThat(cycle.getFirst()).isEqualTo(cycle.getLast());
                });
    }

    /// A stall does not imply a cycle: an edge may point at a vertex that is not part of the graph.
    /// Such an edge can never be discharged, so the sort stalls even though the graph is acyclic.
    ///
    /// This is the shape that [ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider] passes in when it
    /// sorts synthetic entity types: vertices are the *registered* synthetic entities, while dependencies are
    /// collected from query models and filtered by `EntityUtils.isSyntheticEntityType`, which is purely
    /// structural and does not require registration.
    ///
    /// Previously this reached cycle extraction and failed with a [NullPointerException].
    ///
    @Test
    public void a_dangling_edge_is_reported_as_a_dedicated_sort_failure() {
        // `registered` depends on `unregistered`, which is not a vertex -- a dangling edge, and no cycle.
        final var graph = graph("registered", Set.of("unregistered"));

        assertThatThrownBy(() -> sortTopologically(graph))
                .isInstanceOf(TopologicalSortException.class)
                .hasMessageContaining("unregistered")
                .satisfies(ex -> assertThat(cycleOf(ex)).isEmpty());
    }

    /// A malformed graph is reported as such even when it also contains a cycle:
    /// a cycle report would describe a graph that is not well-defined in the first place.
    ///
    @Test
    public void a_dangling_edge_is_reported_in_preference_to_a_cycle() {
        final var graph = graph("open", Set.of("missing"),
                                "a", Set.of("b"),
                                "b", Set.of("a"));

        assertThatThrownBy(() -> sortTopologically(graph))
                .isInstanceOf(TopologicalSortException.class)
                .hasMessageContaining("missing")
                .satisfies(ex -> assertThat(cycleOf(ex)).isEmpty());
    }

    // ------------------------------------------------------------------------------------------------
    // Helpers

    /// The reported cycle, rendered as strings so that it can be asserted on directly.
    ///
    private static List<String> cycleOf(final Throwable ex) {
        return ((TopologicalSortException) ex).cycle().stream().map(String::valueOf).toList();
    }

    /// Builds an adjacency map from alternating vertex / dependency-set arguments.
    ///
    @SuppressWarnings("unchecked")
    private static Map<String, Set<String>> graph(final Object... verticesAndDeps) {
        final Map<String, Set<String>> graph = new LinkedHashMap<>();
        for (int i = 0; i < verticesAndDeps.length; i += 2) {
            graph.put((String) verticesAndDeps[i], (Set<String>) verticesAndDeps[i + 1]);
        }
        return graph;
    }

}
