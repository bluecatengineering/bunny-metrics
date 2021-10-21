# Overview
A sidecar to run alongside Trino to gather metrics using the JMX connector and expose them in different formats using 
Apache Velocity.

Click [here](https://trino.io/docs/current/connector/jmx.html) for more information about JMX connector.

---

# Configuration

## Trino JMX Connector
The sidecar uses Micronaut Framework running on port `8090`.

Sidecar API and all other required values to Trino's JMX connector are located at `/src/resources/application.yml`.

Default values:
```yaml
#...
trino:
  host: "localhost"
  port: 8080
  authentication:
    user: "test"
```
## Exporters and metrics
Exporters configuration files are located under `/src/resources/` folder using [YAML](https://yaml.org/) syntax.

For each exporter, all its metric configurations are loaded underneath its `name` property folder.

Example:
```
/src/resources/exporter/prometheus.yml          <-- Prometheus exporter config file
/src/resources/exporter/prometheus/runtime.yml  <-- Metric config file loaded into Prometheus
/src/resources/exporter/prometheus/xyz.yml      <-- Another config file loaded into Prometheus
```

# API endpoints

## List all available JMX MBeans

---
### Request
`GET /mbeans`

    curl -i -H 'Accept: application/json' http://localhost:8090/mbeans

### Response
```json
[
    "com.sun.management:type=diagnosticcommand",
    "com.sun.management:type=hotspotdiagnostic",
    "io.airlift.discovery.client:name=announcer",
    "io.airlift.discovery.client:name=serviceinventory",
    "io.airlift.http.client:name=fordiscoveryclient,type=httpclient",
    "io.airlift.http.server:context=http_1_1_h2c@3e5cbcfe,id=0,type=httpserverchannellistener",
    "io.airlift.http.server:name=requeststats",
    "io.airlift.jmx:name=stacktracembean",
    "io.airlift.stats:name=gcmonitor",
    "io.airlift.stats:name=pausemeter",
    "java.lang:name=metaspace manager,type=memorymanager",
    "java.lang:name=metaspace,type=memorypool",
    "java.lang:type=memory",
    "java.lang:type=operatingsystem",
    "java.lang:type=runtime",
    "java.lang:type=threading"
]
```

## Fetch available MBean attributes from given MBean name

---

### Request
`GET /mbean/{mBeanName}`

    curl -i -H 'Accept: application/json' 'http://localhost:8090/mbean/java.lang:type=runtime'

### Response
```json
[
    {
        "vmversion": "11.0.12+7-LTS",
        "systemproperties": "javax.management.openmbean.TabularDataSupport(tabularType=javax.management.openmbean.T...",
        "classpath": "/usr/lib/trino/lib/joda-time-2.10.10.jar:/usr/lib/trino/lib/jackson-datatype-guava-2.12.3.jar...",
        "specname": "Java Virtual Machine Specification",
        "objectname": "java.lang:type=Runtime",
        "managementspecversion": "2.0",
        "pid": "1",
        "starttime": "1634822189154",
        "vmname": "OpenJDK 64-Bit Server VM",
        "specvendor": "Oracle Corporation",
        "uptime": "5044819",
        "vmvendor": "Azul Systems, Inc.",
        "node": "8896abd3b013",
        "bootclasspath": null,
        "librarypath": "/usr/java/packages/lib:/usr/lib64:/lib64:/lib:/usr/lib",
        "object_name": "java.lang:type=Runtime",
        "name": "1@8896abd3b013",
        "bootclasspathsupported": "false",
        "inputarguments": "[-Xmx1G, -XX:-UseBiasedLocking, -XX:+UseG1GC, -XX:G1HeapRegionSize=32M, -XX:+ExplicitGCI...",
        "specversion": "11"
    }
]
```

## Export MBean configured metrics from a specific exporter

---

### Request
`GET /metrics/{exporter}`

    curl -i -H 'Accept: application/json' 'http://localhost:8090/metrics/prometheus'

### Response
```
# HELP java_lang_type_runtime_total Amount of time VM is up
# TYPE java_lang_type_runtime_total GAUGE
java_lang_type_runtime_total 5107591
```