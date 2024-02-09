package bunny.metrics.exporter.prometheus.trino;

import org.junit.jupiter.api.Test;

public class QueriesTest extends BaseMetricTest {

    @Override
    protected String getYamlCollectorFile() {
        return "trino_queries";
    }

    @Override
    protected String getJsonRepositoryFile() {
        return "trino_queries.json";
    }

    @Test
    public void runningQueries() {
        assertMetricContains(
            "# HELP trino_running_queries_count Trino Running Queries\n" +
            "# TYPE trino_running_queries_count count\n" +
            "trino_running_queries_count 333"
        );
    }

    @Test
    public void pendingQueries() {
        assertMetricContains(
            "# HELP trino_queries_started_within_five_minutes_count Queries Started Within 5 Minutes\n" +
                "# TYPE trino_queries_started_within_five_minutes_count count\n" +
                "trino_queries_started_within_five_minutes_count 9.5016044"
        );
    }
}
