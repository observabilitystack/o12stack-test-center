# O12Stack Test Center Application

This is a small demo application that can be used for test or training purposes regarding Observability.

## What does this demo do?

This demo processes 'jobs' (small dummy tasks) using a thread pool with workers.

Via a UI you can control the flow of jobs.

## Observability

What is observable about this application?

* Metrics: Prometheus endpoint
* Logs: JSON-one-liners via stdout

### Metrics

(TBD)

### Logs

(TBD)

## Usage / Demo

In our workshops, this application will be run inside a Kubernetes cluster. 
Within this repo, we provide a local demo based on `docker-compose`. The usage in Kubernetes can be easily (well, kind of ...) derived from this demo.

### Build application

Build the application including the Docker image:

    mvn clean package

### Run demo

The resources for this demo can be found in `./local-demo/`:

Spin up the stack with:

```
docker-compose up
```

The stack consists of the demo app, Prometheus and Grafana.

The UI of the demo app can be accessed via http://localhost:8080.

**Prometheus** is configured by mounting the `prometheus.yml` in which the scraping of our demo application is set up. You can access Prometheus via http://localhost:9090.

**Grafana** can be accessed via http://localhost:3000. The initial username/pwd is `admin`/`admin`, you are requested to change it, but that step can be skipped. 

The demo configures the datasource for Prometheus as well as an example dashboard (named **o12stack-test-center**) which displays some metrics of the test app.

Go play around with the stuff ...


[rules-of-a-threadpoolexecutor-pool-size]: http://www.bigsoft.co.uk/blog/2009/11/27/rules-of-a-threadpoolexecutor-pool-size
