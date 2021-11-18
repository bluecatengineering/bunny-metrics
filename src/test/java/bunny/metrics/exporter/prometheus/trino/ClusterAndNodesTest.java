package bunny.metrics.exporter.prometheus.trino;

import org.junit.jupiter.api.Test;

public class ClusterAndNodesTest extends BaseMetricTest {

    @Test
    public void freeMemory() {
        assertMetricContains(
            "# HELP trino_nodes_free_bytes Free memory (general pool)\n" +
            "# TYPE trino_nodes_free_bytes GAUGE\n" +
            "trino_nodes_free_bytes 751619277"
        );
    }

    @Override
    protected String getYamlCollectorFile() {
        return "trino_cluster_nodes";
    }

    @Override
    protected String getJsonRepositoryFile() {
        return "trino_cluster_nodes.json";
    }
}
