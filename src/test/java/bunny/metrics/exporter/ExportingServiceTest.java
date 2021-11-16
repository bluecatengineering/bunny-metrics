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
        Assertions.assertEquals(
            MBEAN_ATTRIBUTE_VALUE,
            exportingService("${mbean.properties." + MBEAN_ATTRIBUTE_KEY + "}").export()
        );
    }

    @Test
    void propertyMetricValuesShouldBeAvailableWithinContext() {
        Assertions.assertEquals(
            FIRST_METRIC_ATTRIBUTE_VALUE,
            exportingService("${metric." + FIRST_METRIC_ATTRIBUTE_KEY + "}").export()
        );
    }

    @Test
    void propertyExporterShouldBeAvailableWithinContext() {
        Assertions.assertEquals(
            Exporter.class.toString(),
            exportingService("${exporter.getClass()}").export()
        );
    }

    @Test
    void defaultExecutePropertyHasToBeEvaluatedBeforeExporterOutput() {
        Assertions.assertEquals(
            "foo" + FIRST_METRIC_ATTRIBUTE_VALUE,
            exportingService(
                "foo",
                null,
                "${metric." + FIRST_METRIC_ATTRIBUTE_KEY + "}"
            ).export()
        );
    }

    @Test
    void metricOutputShouldBeEvaluatedInsteadOfExporterOutput() {
        Assertions.assertEquals(
            "bar" + MBEAN_ATTRIBUTE_VALUE,
            exportingService(
                "bar",
                "${metric." + FIRST_METRIC_ATTRIBUTE_KEY + "}",
                "${mbean.properties." + MBEAN_ATTRIBUTE_KEY + "}"
            ).export()
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
                public Optional<MBean> getMBean(String mBean, boolean onlyHistory) {
                    return Optional.of(new MBean(ANY_BEAN_NAME,Map.of(MBEAN_ATTRIBUTE_KEY, MBEAN_ATTRIBUTE_VALUE)));
                }

                @Override
                public List<String> getAllMBeansNames() {
                    return null;
                }
            }
        );
    }
}