package bunny.metrics.exporter;

import bunny.metrics.jmx.MBeanCallbackListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class ExporterTest {

    @Test
    void shouldRetrieveConfigurationOutputAsDefault() {
        String expected = "defaultOutput";
        ExporterConfiguration exporterConfiguration = new ExporterConfiguration();
        exporterConfiguration.output = expected;
        Assertions.assertEquals(
            expected,
            new Exporter(exporterConfiguration, List.of(new Collector())).output()
        );
    }

    @Test
    void shouldProvideCallbackForMBean() {
        String exporterOutput = "exporterOutput";
        String exporterName = "exporterName";
        String exporterExecute = "exporterExecute";

        ExporterConfiguration exporterConfiguration = new ExporterConfiguration();
        exporterConfiguration.output = exporterOutput;
        exporterConfiguration.name = exporterName;
        exporterConfiguration.execute = exporterExecute;

        String anyBeanName = "any.bean.name";
        Collector collector = new Collector();
        collector.setBeanName(anyBeanName);
        collector.setName("Any bean collector for metrics");

        Metric firstMetric = new Metric();
        firstMetric.setName("First metric");
        firstMetric.setOutput("First metric override output");
        String firstMetricAttributeName = "firstMetricAttributeName";
        String firstMetricAttributeValue = "firstMetricAttributeValue";
        firstMetric.setAttributes(Map.of(firstMetricAttributeName, firstMetricAttributeValue));

        Metric secondMetric = new Metric();
        secondMetric.setName("Second metric");
        secondMetric.setOutput("Second metric override output");
        String secondMetricAttributeName = "secondMetricAttributeName";
        String secondMetricAttributeValue = "secondMetricAttributeValue";
        firstMetric.setAttributes(Map.of(secondMetricAttributeName, secondMetricAttributeValue));
        List<Metric> metricsList = List.of(firstMetric, secondMetric);

        collector.setMetrics(metricsList);

        Map<String, List<MBeanCallbackListener>> mBeanCallbackListeners = new Exporter(
            exporterConfiguration, List.of(collector)
        ).getMBeanCallbackListeners();

        Assertions.assertEquals(
            metricsList.size(),
            mBeanCallbackListeners.get(anyBeanName).size(),
            "Expected same number of listeners for each metric"
        );

        AtomicInteger i = new AtomicInteger();
        mBeanCallbackListeners.get(anyBeanName).forEach(
            mBeanCallbackListener -> {
                Metric metric = metricsList.get(i.getAndIncrement());
                Assertions.assertEquals(
                    exporterExecute,
                    mBeanCallbackListener.getExecuteTemplate().orElse("")
                );
                Assertions.assertTrue(
                    mBeanCallbackListener.getTemplateTag().contains(collector.toString()),
                    "Template tag has to contain collector string representation"
                );
                Assertions.assertTrue(
                    mBeanCallbackListener.getTemplateTag().contains(exporterConfiguration.toString()),
                    "Template tag has to contain exporter configuration string representation"
                );
                Assertions.assertTrue(
                    mBeanCallbackListener.getTemplateTag().contains(metric.toString()),
                    "Template tag has to contain metric string representation"
                );
                Assertions.assertEquals(
                    mBeanCallbackListener.getOutputTemplate(),
                    metric.output,
                    "Metric specified output should be retrieved instead of configuration"
                );
                Assertions.assertEquals(
                    mBeanCallbackListener.getMetricValues(),
                    metric.getAttributes()
                );
                Assertions.assertEquals(
                    mBeanCallbackListener.shouldFetchOnlyHistory(),
                    collector.history
                );
            }
        );
    }
}