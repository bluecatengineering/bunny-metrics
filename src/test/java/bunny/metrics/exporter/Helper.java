package bunny.metrics.exporter;

import bunny.metrics.jmx.entity.MBean;
import bunny.metrics.jmx.repository.MBeansRepository;
import com.google.gson.Gson;
import io.micronaut.core.io.ResourceLoader;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public class Helper {
    public static ExporterConfiguration loadPrometheusExporter(ResourceLoader resourceLoader) {
        return new Yaml(new Constructor(ExporterConfiguration.class)).load(
            resourceLoader.getResourceAsStream("exporters/prometheus.yml").orElseThrow()
        );
    }
    public static Collector loadPrometheusCollector(ResourceLoader resourceLoader, String metric) {
        return new Yaml(new Constructor(Collector.class)).load(
            resourceLoader.getResourceAsStream("exporters/prometheus/" + metric + ".yml").orElseThrow()
        );
    }

    public static MBeansRepository jsonRepository(ResourceLoader resourceLoader, String jsonFileName) {
        return new MBeansRepository() {
            @Override
            public List<MBean> getMBean(String mBeanName, boolean onlyHistory) {
                Optional<URL> url = resourceLoader.getResources(jsonFileName).findFirst();
                ArrayList<MBean> mBeanList = new ArrayList<>();
                if (url.isPresent()) {
                    try {
                        for (Map map : new Gson().fromJson(new FileReader(
                            Objects.requireNonNull(Paths.get(url.get().toURI()).toFile())
                        ), Map[].class)) {
                            mBeanList.add(
                                new MBean(mBeanName, map)
                            );
                        }
                    } catch (URISyntaxException | FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                return mBeanList;
            }

            @Override
            public List<String> getAllMBeansNames() {
                return null;
            }
        };
    }
}
