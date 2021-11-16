package bunny.metrics.exporter;

import bunny.metrics.jmx.MBeanCallbackListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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

        collector.setMetrics(List.of(firstMetric));

        Map<String, MBeanCallbackListener> callbackListeners = new Exporter(
            exporterConfiguration, List.of(collector)
        ).getMBeanCallbackListeners();

        callbackListeners.forEach((beanName, mBeanCallbackListener) -> {
            Assertions.assertEquals(
                beanName,
                anyBeanName
            );
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
                mBeanCallbackListener.getTemplateTag().contains(firstMetric.toString()),
                "Template tag has to contain metric string representation"
            );
            Assertions.assertEquals(
                mBeanCallbackListener.getOutputTemplate(),
                firstMetric.output,
                "Metric specified output should be retrieved instead of configuration"
            );
            Assertions.assertEquals(
                mBeanCallbackListener.getMetricValues(),
                firstMetric.getAttributes()
            );
            Assertions.assertEquals(
                mBeanCallbackListener.shouldFetchOnlyHistory(),
                collector.history
            );
        });
    }
}