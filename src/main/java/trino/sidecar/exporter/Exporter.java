package trino.sidecar.exporter;

import trino.sidecar.jmx.MBeanCallbackListener;

import java.util.*;

public class Exporter {
    protected ExporterConfiguration configuration;
    protected List<Collector> listOfCollectors;

    public Exporter(ExporterConfiguration configuration, List<Collector> listOfCollectors) {
        this.configuration = configuration;
        this.listOfCollectors = listOfCollectors;
    }

    public String output() {
        return configuration.output;
    }

    public Map<String, MBeanCallbackListener> getMBeanCallbackListeners() {
        HashMap<String, MBeanCallbackListener> map = new HashMap<>();
        listOfCollectors.forEach(
            collector -> collector.metrics.forEach(
                metric -> map.put(collector.beanName, new MBeanCallbackListener() {
                    @Override
                    public Optional<String> getExecuteTemplate() {
                        return Optional.ofNullable(configuration.execute);
                    }

                    @Override
                    public String getTemplateTag() {
                        return configuration + "__" + collector + "__" + metric;
                    }

                    @Override
                    public String getOutputTemplate() {
                        // Metric or Exporter output template
                        return metric.output != null ? metric.output : configuration.output;
                    }

                    @Override
                    public Map<String, String> getMetricValues() {
                        return metric.attributes;
                    }

                    @Override
                    public boolean shouldFetchOnlyHistory() {
                        return collector.history;
                    }
                })
            )
        );
        return map;
    }
}
