package trino.sidecar.exporter;

import io.micronaut.core.io.ResourceLoader;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import trino.sidecar.jmx.entity.MBean;

import java.io.StringWriter;
import java.util.Map;
import java.util.stream.Stream;

@MicronautTest
@DisplayName("Prometheus exporter")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PrometheusConfigurationTest {
    @Inject
    ResourceLoader resourceLoader;

    private static ExporterConfiguration configuration;

    private static final String TAG = "test_tag";

    @BeforeAll
    void beforeAll() {
        Velocity.init();
        configuration = new Yaml(new Constructor(ExporterConfiguration.class)).load(
            resourceLoader.getResourceAsStream("exporters/prometheus.yml").orElseThrow()
        );
    }

    @Test
    void defaultNameAsBeanName() {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();
        String expected = "test_bean_name";

        // Add a MBean with "testbean" as default value
        context.put("mbean", new MBean(expected, Map.of()));
        // Evaluate 'execute' VLT
        Velocity.evaluate(context, out, TAG, configuration.execute);
        // Print "$name"
        Velocity.evaluate(context, out, TAG, "$name");

        Assertions.assertEquals(expected, out.toString());
    }

    @Test
    void nameWithMetric() {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();
        String expected = "metric_overridden_name";

        // Add a MBean with "testbean" as default value
        context.put("mbean", new MBean("test_bean_name", Map.of()));
        // Add metric k-v "name" as overridden
        context.put("metric", Map.of("name", expected));
        // Evaluate 'execute' VLT
        Velocity.evaluate(context, out, TAG, configuration.execute);
        // Print "$name"
        Velocity.evaluate(context, out, TAG, "$name");

        Assertions.assertEquals(expected, out.toString());
    }

    @Test
    void defaultDescriptionAsBeanName() {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();
        String expected = "test_bean_name";

        context.put("mbean", new MBean(expected, Map.of()));
        Velocity.evaluate(context, out, TAG, configuration.execute);
        Velocity.evaluate(context, out, TAG, "$description");
        Assertions.assertEquals(expected, out.toString());
    }

    @Test
    void descriptionWithMetric() {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();
        String expected = "metric_overridden_description";

        context.put("mbean", new MBean("test_bean_name", Map.of()));
        context.put("metric", Map.of("description", expected));
        Velocity.evaluate(context, out, TAG, configuration.execute);
        Velocity.evaluate(context, out, TAG, "$description");
        Assertions.assertEquals(expected, out.toString());
    }

    @Test
    void defaultTypeAsUntypedWhenMetricObjectDoesNotExist() {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();

        context.put("mbean", new MBean("any_bean_name", Map.of()));
        context.put("metric", Map.of());
        Velocity.evaluate(context, out, TAG, configuration.execute);
        Velocity.evaluate(context, out, TAG, "$type");
        Assertions.assertEquals("untyped", out.toString());
    }
    @Test
    void defaultTypeAsUntyped() {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();

        context.put("mbean", new MBean("any_bean_name", Map.of()));
        Velocity.evaluate(context, out, TAG, configuration.execute);
        Velocity.evaluate(context, out, TAG, "$type");
        Assertions.assertEquals("untyped", out.toString());
    }

    @Test
    void typeWithMetric() {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();
        String expected = "counter";

        context.put("mbean", new MBean("test_bean_name", Map.of()));
        context.put("metric", Map.of("type", expected));
        Velocity.evaluate(context, out, TAG, configuration.execute);
        Velocity.evaluate(context, out, TAG, "$type");
        Assertions.assertEquals(expected, out.toString());
    }

    @Test
    void defaultLabelsWhenNullProperties() {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();

        context.put("mbean", new MBean("any_bean_name", Map.of()));
        context.put("metric", Map.of());
        Velocity.evaluate(context, out, TAG, configuration.execute);
        Velocity.evaluate(context, out, TAG, "$!{labels}");
        Assertions.assertEquals("", out.toString());
    }

    @Test
    void defaultLabelsWhenNotNullMBeanProperties() {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();

        context.put("mbean", new MBean("any_bean_name", Map.of("key", "value")));
        context.put("metric", Map.of());
        Velocity.evaluate(context, out, TAG, configuration.execute);
        Velocity.evaluate(context, out, TAG, "$labels");
        Assertions.assertEquals("{key=value}", out.toString());
    }

    @Test
    void defaultValueIsNull() {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();

        context.put("mbean", new MBean("any_bean_name", Map.of()));
        context.put("metric", Map.of("key_non_related_to_value", "any_value"));
        Velocity.evaluate(context, out, TAG, configuration.execute);
        Velocity.evaluate(context, out, TAG, "$value");
        Assertions.assertEquals("$value", out.toString());
    }

    @Test
    void valueWithMetric() {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();

        context.put("mbean", new MBean("any_bean_name", Map.of()));
        context.put("metric", Map.of("value", 10));
        Velocity.evaluate(context, out, TAG, configuration.execute);
        Velocity.evaluate(context, out, TAG, "$value");
        Assertions.assertEquals("10", out.toString());
    }

    @Test
    void defaultTimestampIsNull() {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();

        context.put("mbean", new MBean("any_bean_name", Map.of()));
        context.put("metric", Map.of("key_non_related_to_value", "any_value"));
        Velocity.evaluate(context, out, TAG, configuration.execute);
        Velocity.evaluate(context, out, TAG, "$timestamp");
        Assertions.assertEquals("$timestamp", out.toString());
    }

    @Test
    void timestampWithMetricValue() {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();
        Long timestamp = System.currentTimeMillis() / 1000L;

        context.put("mbean", new MBean("any_bean_name", Map.of()));
        context.put("metric", Map.of("timestamp", timestamp));
        Velocity.evaluate(context, out, TAG, configuration.execute);
        Velocity.evaluate(context, out, TAG, "$timestamp");
        Assertions.assertEquals(timestamp.toString(), out.toString());
    }

    static Stream<Arguments> sanitizeNameMacroScenarios() {
        return Stream.of(
            Arguments.arguments("aaa%bbb", "aaa_bbb"),
            Arguments.arguments("trino.server:name=taskresource", "trino_server_name_taskresource"),
            Arguments.arguments("0__x_xakgka", "__x_xakgka"),
            Arguments.arguments("__x_beef:number", "__x_beef_number")
        );
    }

    @ParameterizedTest
    @MethodSource("sanitizeNameMacroScenarios")
    void macroShouldSanitizedName(String actual, String expected) {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();

        Velocity.evaluate(context, out, TAG, configuration.execute);
        Velocity.evaluate(context, out, TAG, "#sanName( \"" + actual + "\" )");
        Assertions.assertEquals(expected, out.toString());
    }

    static Stream<Arguments> sanitizeLabelNameMacroScenarios() {
        return Stream.of(
            Arguments.arguments("a.a.a%bbb", "a_a_a_bbb"),
            Arguments.arguments("trino.server:name=taskresource", "trino_server_name_taskresource"),
            Arguments.arguments("0__x_xakgka", "x_xakgka"),
            Arguments.arguments("__x_beef:number", "x_beef_number")
        );
    }

    @ParameterizedTest
    @MethodSource("sanitizeLabelNameMacroScenarios")
    void nameMacroShouldSanitizedLabelName(String actual, String expected) {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();

        Velocity.evaluate(context, out, TAG, configuration.execute);
        Velocity.evaluate(context, out, TAG, "#sanLabelName( \"" + actual + "\" )");
        Assertions.assertEquals(expected, out.toString());
    }

    @Test
    void labelsMacroShouldFormatLabelsFromGivenMap() {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();
        context.put("inputLabels", Map.of("name1", "value1"));

        Velocity.evaluate(context, out, TAG, configuration.execute);
        Velocity.evaluate(context, out, TAG, "#labels( $inputLabels )");
        Assertions.assertEquals("{name1=\"value1\",}", out.toString());
    }

    @ParameterizedTest
    @MethodSource("sanitizeLabelNameMacroScenarios")
    void labelsMacroShouldSanitizeKeyFromGivenMap(String actual, String expected) {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();
        context.put("inputLabels", Map.of(actual, "value1"));

        Velocity.evaluate(context, out, TAG, configuration.execute);
        Velocity.evaluate(context, out, TAG, "#labels( $inputLabels )");
        Assertions.assertEquals("{" + expected + "=\"value1\",}", out.toString());
    }

    @Test
    void outputShouldSanitizeValues() {
        StringWriter out = new StringWriter();
        VelocityContext context = new VelocityContext();
        context.put("mbean", new MBean("any$bean%name", Map.of(
                "$@!name_one_", "value_one6555"
        )));

        Velocity.evaluate(context, out, TAG, configuration.execute);
        Velocity.evaluate(context, out, TAG, configuration.output);
        Assertions.assertEquals("# HELP any_bean_name any$bean%name\n" +
                "# TYPE any_bean_name untyped\n" +
                "any_bean_name{name_one_=\"value_one6555\",}", out.toString());
    }
}