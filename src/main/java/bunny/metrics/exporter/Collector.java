package bunny.metrics.exporter;

import java.util.List;

public class Collector {
    String name;
    String beanName;
    List<Metric> metrics;
    public boolean history;

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }

    @Override
    public String toString() {
        return "collector-" + name;
    }
}
