package bunny.metrics.exporter;

public class ExporterConfiguration {
    public String name;
    public String output;
    public String execute;

    @Override
    public String toString() {
        return "exporter-" + name;
    }
}
