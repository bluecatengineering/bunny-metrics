package bunny.metrics.health;

import bunny.metrics.jmx.repository.TrinoMBeansRepository;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.micronaut.management.health.indicator.AbstractHealthIndicator;
import io.micronaut.management.health.indicator.annotation.Liveness;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Singleton
@Requires(beans = HealthEndpoint.class)
@Liveness
public class  BunnyMetricsHealthIndicator extends AbstractHealthIndicator<Map<String, Object>> {
    public static final String NAME = "trino";
    private static final Logger LOG = LoggerFactory.getLogger(BunnyMetricsHealthIndicator.class);

    @Property(name = "trino.host")
    @NotBlank
    protected String trinoHost;

    @Property(name = "trino.port")
    @NotBlank
    protected String trinoPort;

    @Property(name = "trino.jdbc-properties")
    @NotBlank
    protected HashMap<String, String> jdbcProperties;

    private Map<String, Object> processError(Throwable throwable) {
        LOG.warn("Could not connect to Trino", throwable);
        Map<String, Object> healthInformation = new LinkedHashMap<>();
        healthInformation.put("error", throwable.getMessage());
        this.healthStatus = HealthStatus.UNKNOWN;
        return healthInformation;
    }

    @Override
    protected Map<String, Object> getHealthInformation() {
        try {
            TrinoMBeansRepository.factory(trinoHost, trinoPort, jdbcProperties);
            this.healthStatus = HealthStatus.UP;
            return Collections.emptyMap();
        } catch (Exception exception) {
            return processError(exception);
        }
    }

    @Override
    protected String getName() {
        return NAME;
    }
}
