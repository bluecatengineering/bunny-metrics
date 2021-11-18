package bunny.metrics.exporter;

import bunny.metrics.jmx.entity.MBean;
import bunny.metrics.jmx.repository.MBeansRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExportingServiceTest {

    private static final String ANY_BEAN_NAME = "any.bean.name";
    private static final String FIRST_METRIC_ATTRIBUTE_KEY = "firstMetricAttributeKey";
    private static final String FIRST_METRIC_ATTRIBUTE_VALUE = "firstMetricAttributeValue";
    private static final String MBEAN_ATTRIBUTE_KEY = "colOne";
    private static final String MBEAN_ATTRIBUTE_VALUE = "ColOneValue";

    @Test
    void propertyMBeanShouldBeAvailableWithinContext() {
        Assertions.assertTrue(
            exportingService("${mbean.properties." + MBEAN_ATTRIBUTE_KEY + "}").export().contains(MBEAN_ATTRIBUTE_VALUE)
        );
    }

    @Test
    void propertyMetricValuesShouldBeAvailableWithinContext() {
        Assertions.assertTrue(
            exportingService("${metric." + FIRST_METRIC_ATTRIBUTE_KEY + "}").export().contains(FIRST_METRIC_ATTRIBUTE_VALUE)
        );
    }

    @Test
    void propertyExporterShouldBeAvailableWithinContext() {
        Assertions.assertTrue(
            exportingService("${exporter.getClass()}").export().contains(Exporter.class.toString())
        );
    }

    @Test
    void defaultExecutePropertyHasToBeEvaluatedBeforeExporterOutput() {
        Assertions.assertTrue(
            exportingService(
                "foo",
                null,
                "${metric." + FIRST_METRIC_ATTRIBUTE_KEY + "}"
            ).export().contains("foo" + FIRST_METRIC_ATTRIBUTE_VALUE)
        );
    }

    @Test
    void metricOutputShouldBeEvaluatedInsteadOfExporterOutput() {
        Assertions.assertTrue(
            exportingService(
                "bar",
                "${metric." + FIRST_METRIC_ATTRIBUTE_KEY + "}",
                "${mbean.properties." + MBEAN_ATTRIBUTE_KEY + "}"
            ).export().contains("bar" + MBEAN_ATTRIBUTE_VALUE)
        );
    }

    private Collector buildCollector(@Nullable String metricOutput) {
        Collector collector = new Collector();
        collector.setBeanName(ANY_BEAN_NAME);
        collector.setName("Any bean collector for metrics");

        Metric firstMetric = new Metric();
        firstMetric.setName("First metric");
        firstMetric.setOutput(Optional.ofNullable(metricOutput).orElse(""));
        firstMetric.setAttributes(Map.of(FIRST_METRIC_ATTRIBUTE_KEY, FIRST_METRIC_ATTRIBUTE_VALUE));

        collector.setMetrics(List.of(firstMetric));

        return collector;
    }

    private ExporterConfiguration buildExporterConfiguration(String execute, @Nullable String exporterOutput) {
        ExporterConfiguration exporterConfiguration = new ExporterConfiguration();
        exporterConfiguration.output = Optional.ofNullable(exporterOutput).orElse("");
        exporterConfiguration.name = "name";
        exporterConfiguration.execute = execute;
        return exporterConfiguration;
    }

    private ExportingService exportingService(String exporterExecute) {
        return exportingService(exporterExecute, null, null);
    }

    private ExportingService exportingService(String exporterExecute, @Nullable String exporterOutput, @Nullable String metricOutput) {
        return new ExportingService(
            new Properties(),
            new Exporter(buildExporterConfiguration(exporterExecute, exporterOutput), List.of(buildCollector(metricOutput))),
            new MBeansRepository() {
                @Override
                public List<MBean> getMBean(String mBean, boolean onlyHistory) {
                    return List.of(new MBean(ANY_BEAN_NAME,Map.of(MBEAN_ATTRIBUTE_KEY, MBEAN_ATTRIBUTE_VALUE)));
                }

                @Override
                public List<String> getAllMBeansNames() {
                    return null;
                }
            }
        );
    }
}