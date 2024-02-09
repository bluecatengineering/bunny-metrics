package bunny.metrics.exporter;

import io.micronaut.core.io.ResourceLoader;
import jakarta.inject.Singleton;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

@Singleton
public class LoaderService {
    private final ResourceLoader resourceLoader;
    private final HashMap<String, Exporter> exporters = new HashMap<>();

    private static final String CONFIG_EXTENSION = ".yml";
    private static final String DIR_EXPORTER = System.getenv().getOrDefault("BUNNY_METRICS_EXPORTERS_DIR", "exporters") + File.separator;
    private static final String METRICS_DIR = System.getenv().getOrDefault("BUNNY_METRICS_METRICS_DIR", "metrics") + File.separator;

    public LoaderService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        loadExporters();
    }

    protected void loadExporters() {
        // Locate all exporters
        invokePerEachFileFound(
            DIR_EXPORTER,
            // For each exporter found...
            exporterConfigFileFound -> {
                try {
                    ExporterConfiguration exporterConfig = loadYaml(
                        ExporterConfiguration.class,
                        exporterConfigFileFound
                    );
                    //...map its metrics configs
                    exporters.put(
                        exporterConfig.name,
                        new Exporter(
                            exporterConfig,
                            loadMetricsConfiguration(exporterConfig.name)
                        )
                    );
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        );
    }

    private <T> T loadYaml(Class<T> classType, File config) throws FileNotFoundException {
        return new Yaml(
            new Constructor(classType)
        ).load(new FileInputStream(config));
    }

    private List<Collector> loadMetricsConfiguration(String exporterName) {
        ArrayList<Collector> collectors = new ArrayList<>();
        // Load metrics for each exporter
        invokePerEachFileFound(
            METRICS_DIR + exporterName + File.separator,
            collectorFile -> {
                try {
                    // Load and add them
                    collectors.add(
                        loadYaml(Collector.class, collectorFile)
                    );
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        );
        return collectors;
    }

    protected void invokePerEachFileFound(String rootFolder, PerEachFileLocated func) {
        Optional<URL> url = resourceLoader.getResources(rootFolder).findAny();
        if (url.isPresent()) {
            try {
                Arrays.stream(
                    Objects.requireNonNull(Paths.get(url.get().toURI()).toFile().listFiles())
                )
                .filter(File::isFile)
                .filter(file -> file.getName().endsWith(LoaderService.CONFIG_EXTENSION))
                .forEach(func::invoke);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public Exporter getExporter(String exporterName) {
        if (!exporters.containsKey(exporterName)) {
            throw new IllegalArgumentException("Exporter \"" + exporterName + "\"does not exist");
        }

        return exporters.get(exporterName);
    }

    interface PerEachFileLocated {
        void invoke(File fileFound);
    }
}

