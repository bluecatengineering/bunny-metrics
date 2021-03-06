package bunny.metrics.exporter;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import bunny.metrics.jmx.MBeanCallbackListener;
import bunny.metrics.jmx.repository.MBeansRepository;

import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

public class ExportingService {
    private static final int LIMIT_TAG_LENGTH = 30;
    private static final String KEY_OBJECT_MBEAN = "mbean";
    private static final String KEY_OBJECT_EXPORTER = "exporter";
    private static final String KEY_OBJECT_METRIC_VALUES = "metric";

    private final Exporter exporter;
    private final MBeansRepository repository;

    public ExportingService(Properties apacheProperties, Exporter exporter, MBeansRepository repository) {
        this.exporter = exporter;
        this.repository = repository;
        Velocity.init(apacheProperties);
    }

    public String export() {
        StringWriter output = new StringWriter();

        // Fetch all MBean callback
        exporter.getMBeanCallbackListeners().forEach((String mBeanName, List<MBeanCallbackListener> listOfListeners) ->
            listOfListeners.forEach(listener ->
                repository.getMBean(mBeanName, listener.shouldFetchOnlyHistory()).forEach(mBean -> {
                    // Add default Map values providing
                    VelocityContext exporterContext = new VelocityContext();
                    // Add found MBean to collector's context
                    exporterContext.put(KEY_OBJECT_MBEAN, mBean);
                    // Add exporter to that context for metric usage
                    exporterContext.put(KEY_OBJECT_EXPORTER, exporter);
                    // Add metric data to be available during execution
                    exporterContext.put(KEY_OBJECT_METRIC_VALUES, listener.getMetricValues());
                    // Evaluate default Export's "execute" YML property
                    listener.getExecuteTemplate().ifPresent(
                        executeTemplate -> Velocity.evaluate(
                            exporterContext,
                            output,
                            listener.getTemplateTag().substring(0, LIMIT_TAG_LENGTH),
                            executeTemplate
                        )
                    );
                    // Retrieve template to generate output
                    Velocity.evaluate(
                        exporterContext,
                        output,
                        listener.getTemplateTag().substring(0, LIMIT_TAG_LENGTH),
                        listener.getOutputTemplate()
                    );
                    output.append("\n");
                })
            )
        );

        return output.toString();
    }
}
