# O12Stack Test Center Application

This is a small demo application that can be used for test or training purposes regarding Observability.

## What does this demo do?

This demo processes 'jobs' (small math tasks) using a 4-piece thread pool with workers.

Via a UI (as of now a pretty basic one) you can control the flow of jobs.

## Observability

What is observable about this application?

* Metrics: Prometheus endpoint
* Logs: JSON-one-liners via stdout (TBD)

### Metrics

(TBD)

### Logs

(TBD)

## Usage

In our workshops, this application will be run inside a Kubernetes cluster. We also provide a local demo, based on `docker-compose`. Both are described in the following subsections.

### Local Demo

#### Build application

Build the application including the Docker image:

    mvn clean package

#### Run demo

The resources for this demo can be found in `./local-demo/`:

Spin up the stack with:

```
docker-compose up
```

The stack consists of the demo app, Prometheus and Grafana.

The UI of the demo app can be accessed via http://localhost:8080.

**Prometheus** is configured by mounting the `prometheus.yml` in which the scraping of our demo application is set up. You can access Prometheus via http://localhost:9090.

**Grafana** can be accessed via http://localhost:3000. The initial username/pwd is `admin`/`admin`, you are requested to change it. Remember, it's just a local demo, so don't put too much effort in that. 

You have to configure a Prometheus Data Source using http://prometheus:9090, which is the correct address of Prometheus inside the Docker compose network.

You can import a Dashboard from the file `grafana-dashboard.json`. It provides some examples for visualizations of metrics.

Go play around with the stuff ...


### Kubernetes

(TBD)
