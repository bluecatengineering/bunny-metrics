package bunny.metrics.exporter.prometheus.trino;

import bunny.metrics.exporter.Exporter;
import bunny.metrics.exporter.ExportingService;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Properties;

import static bunny.metrics.exporter.Helper.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract public class BaseMetricTest {

    @Inject
    protected ResourceLoader resourceLoader;
    private ExportingService exportingService;

    @BeforeAll
    void beforeAll() {
        exportingService = new ExportingService(
            new Properties(),
            new Exporter(
                loadPrometheusExporter(resourceLoader),
                List.of(loadPrometheusCollector(resourceLoader,getYamlCollectorFile()))
            ),
            jsonRepository(resourceLoader, getJsonRepositoryFile())
        );
    }

    abstract protected String getYamlCollectorFile();
    abstract protected String getJsonRepositoryFile();

    void assertMetricContains(String searchString) {
        Assertions.assertTrue(exportingService.export().contains(searchString));
    }
}
