package bunny.metrics;

import bunny.metrics.exporter.ExportingService;
import bunny.metrics.exporter.LoaderService;
import bunny.metrics.jmx.repository.TrinoMBeansRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@Controller()
public class MainController {

    @Property(name = "velocityProperties")
    @Nullable
    protected Map<String, String> velocityProperties;

    @Property(name = "trino.host")
    @NotBlank
    protected String trinoHost;

    @Property(name = "trino.port")
    @NotBlank
    protected String trinoPort;

    @Property(name = "trino.authentication")
    @NotBlank
    protected HashMap<String, String> authentication;

    private TrinoMBeansRepository repository;
    private final LoaderService loaderService;

    public MainController(LoaderService loaderService) {
        this.loaderService = loaderService;
    }

    @Get(uri = "/metrics/{exporter}", produces = {MediaType.TEXT_PLAIN})
    public String metrics(String exporter) {
        Properties apacheVelocityProperties = new Properties();
        Objects.requireNonNull(velocityProperties).forEach(apacheVelocityProperties::setProperty);

        return new ExportingService(
            apacheVelocityProperties,
            loaderService.getExporter(exporter),
            getRepository()
        ).export();
    }

    @Get(uri = "/mbeans", consumes = {MediaType.APPLICATION_JSON}, produces = {MediaType.APPLICATION_JSON})
    public String mBeans() {
        JsonArray jsonResultArray = new JsonArray();
        getRepository().getAllMBeansNames().forEach(jsonResultArray::add);
        return jsonResultArray.size() > 0 ? jsonResultArray.toString() : null;
    }

    @Get(uri = "/mbean/{mBeanName}", consumes = {MediaType.APPLICATION_JSON}, produces = {MediaType.APPLICATION_JSON})
    public String mBean(String mBeanName) {
        try {
            return fetchMBeanResults(mBeanName).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private TrinoMBeansRepository getRepository() {
        if (repository == null) {
            // TODO inject factory inside constructor using Micronaut build-in structure
            repository = TrinoMBeansRepository.factory(trinoHost, trinoPort, authentication);
        }
        return repository;
    }

    private JsonArray fetchMBeanResults(String mBeanName) {
        JsonArray jsonArray = new JsonArray();
        getRepository().getMBean(mBeanName, false).ifPresent(mBean -> {
            JsonObject properties = new JsonObject();
            mBean.getProperties().forEach(properties::addProperty);
            jsonArray.add(properties);
        });
        return jsonArray;
    }
}
