package bunny.metrics.jmx;

import java.util.Map;
import java.util.Optional;

public interface MBeanCallbackListener {
    /**
     * Retrieve a key-value map with default values related to that MBean
     */
    Optional<String> getExecuteTemplate();

    /**
     * Retrieve a template tag (unique ID)
     */
    String getTemplateTag();

    /**
     * Retrieve a VTL template in String to render the output
     */
    String getOutputTemplate();

    /**
     * Retrieve metric values provided by its config file
     */
    Map<String, String> getMetricValues();

    /**
     * MBean data source
     */
    boolean shouldFetchOnlyHistory();
}
