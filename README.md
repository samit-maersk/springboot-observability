# springboot-observability

Inspired From:

- [https://tanzu.vmware.com/developer/guides/observability-reactive-spring-boot-3/](https://tanzu.vmware.com/developer/guides/observability-reactive-spring-boot-3/)
- [https://github.com/blueswen/spring-boot-observability](https://github.com/blueswen/spring-boot-observability)
- [micrometer](https://micrometer.io/docs/observation)
- [loki](https://grafana.com/docs/loki/latest/logql/metric_queries/)
- [prometheus](https://prometheus.io/docs/prometheus/latest/querying/examples/)
- [jsonplaceholder](https://jsonplaceholder.typicode.com/) To cover WebClient scenario.
- [about springboot prometheus metrics](https://tanzu.vmware.com/developer/guides/observability-reactive-spring-boot-3/)
- [custom metric for counter, gauge , Timer & Summary](https://autsoft.net/en/defining-custom-metrics-in-a-spring-boot-application-using-micrometer/)
- [Types of metrics in prometheus](https://www.youtube.com/watch?v=nJMRmhbY5hY&list=LL&index=2)



Run the below script to get some metrics: 

```shell
while true; do curl http://localhost:8080/hello?type=FIRSTNAME ; sleep 1; done
localhost:8080/user/3
```

## The 4 Types Of Prometheus Metrics

**Counters**
- When you want to record a vaule that only goes up
- When you want to be able to later query how fast the value is increasing
- example: request count, tasks completed, error count
```java
var requestCount= Counter.build()
        .name("request_count")
        .help("Number of request count")
        .register(collectorRegistry);
```
- In the prometheus metrics we can see this counter metrics something like this `request_count 22.5`
- promQL for counter will be
```
rate(request_count[5m])
```

**Gauges**
- When you want to record a value that can go up and down
- When you don't need to query its rate
- Example: memory usage, queue size and number of request in progress
```java
var queueSize= Gauge.build()
        .name("queue_size")
        .help("size of the queue")
        .register(collectorRegistry);

queueSize.inc()

queueSize.dec()

```
- The metrics will looks like `queue_size 8.0` 
- promQL for counter will be
```
avg_over_time(queue_size[5m])
```
**Histograms**
- When you want to take many measurements of a value, to later calculate average or percentiles.
- When you are not bothred about the exect values, but are happy with an approximation.
- When you know what the range of value will be up front, so can use the default bucket definitions or define your own.
- Example: request duration, response size
```java
var requestDuration = Histogram.build()
        .name("request_duration")
        .help("Time for http request")
        .register(collectorRegistry);

Histogram.Timer timer = requestDuration.startTimer()
        ...
timer.observeDuration()
```
- The metrics will look like:
```
request_duration_bucket{le="0.005",} 0.0
....
....
request_duration_bucket{le="5.0",} 3.0
request_duration_bucket{le="+Inf",} 3.0
....
# This allow calulation avg and percentile
request_duration_count 3.0
request_duration_sum 16.0779359
```
- promQL
avg request duration for last 5m
```
rate(request_duration_sum[5m])/rate(request_duration_count[5m])
```
percentile calculation
```
histogram_quantile(.95,sum(rate(rquest_duration_bucket[5m])) by (le))
```

**Summaries**
Histograms and Summaries are quite similar but it's recommend to use Histograms based on the situation and requirements.