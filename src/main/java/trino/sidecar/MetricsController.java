package trino.sidecar;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import trino.sidecar.exporter.LoaderService;
import trino.sidecar.exporter.ExportingService;
import trino.sidecar.jmx.repository.TrinoMBeansRepository;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;

@Controller()
public class MetricsController {

    @Property(name = "trino.host")
    @NotBlank
    String trinoHost;

    @Property(name = "trino.port")
    @NotBlank
    String trinoPort;

    @Property(name = "trino.authentication")
    @NotBlank
    HashMap<String, String> authentication;

    private TrinoMBeansRepository repository;
    private final LoaderService loaderService;

    public MetricsController(LoaderService loaderService) {
        this.loaderService = loaderService;
    }

    @Get(uri = "/metrics/{exporter}", produces = {MediaType.TEXT_PLAIN})
    public String metrics(String exporter) {
        return new ExportingService(loaderService.getExporter(exporter), getRepository()).export();
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
