<!-- build-status -->
[![Build](https://travis-ci.com/remal/tracing-spec.svg?branch=master)](https://travis-ci.com/github/remal/tracing-spec)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/name.remal.tracing-spec/tracing-spec-application/badge.svg)](https://maven-badges.herokuapp.com/maven-central/name.remal.tracing-spec/tracing-spec-application)

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=name.remal.tracing-spec%3Atracing-spec&metric=coverage)](https://sonarcloud.io/dashboard?id=name.remal.tracing-spec%3Atracing-spec)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=name.remal.tracing-spec%3Atracing-spec&metric=alert_status)](https://sonarcloud.io/dashboard?id=name.remal.tracing-spec%3Atracing-spec)
* [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=name.remal.tracing-spec%3Atracing-spec&metric=security_rating)](https://sonarcloud.io/dashboard?id=name.remal.tracing-spec%3Atracing-spec)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=name.remal.tracing-spec%3Atracing-spec&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=name.remal.tracing-spec%3Atracing-spec)
* [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=name.remal.tracing-spec%3Atracing-spec&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=name.remal.tracing-spec%3Atracing-spec)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=name.remal.tracing-spec%3Atracing-spec&metric=bugs)](https://sonarcloud.io/dashboard?id=name.remal.tracing-spec%3Atracing-spec)
* [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=name.remal.tracing-spec%3Atracing-spec&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=name.remal.tracing-spec%3Atracing-spec)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=name.remal.tracing-spec%3Atracing-spec&metric=code_smells)](https://sonarcloud.io/dashboard?id=name.remal.tracing-spec%3Atracing-spec)
<!--/ build-status -->

# TracingSpec

<!-- description -->
<img align="left" width="100" height="100" src="https://raw.githubusercontent.com/remal/tracing-spec/master/logo.svg" alt="logo">

Sometimes you have really complex business scenarios with a lot of different services involved. You can write documentation manually, but there are some problems here:
1. The documentation should be maintained, which is hard in active development stage of the project
1. How to validate it?

This projects helps to solve such problems by handling data from distributed tracing system (e.g. [Zipkin](https://zipkin.io/), [Jaeger](https://www.jaegertracing.io/)) and providing visualization and validation functionality

<br clear="left">
<!--/ description -->

## Application usage

Download [tracing-spec-app.jar](#) and use it as a general Spring Boot application.

### Configuration

The application should be configured using [Spring Boot Externalized Configuration feature](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config). You can configure these properties:

<!-- properties -->
| Property | Description | Type | Default value |
| --- | --- | --- | --- |
| `tracingspec.renderer.options.graph-processors` | SpecSpansGraph processors | `List<SpecSpansGraphProcessor>` |  |
| `tracingspec.renderer.options.node-processors` | SpecSpanNode processors | `List<SpecSpanNodeProcessor>` |  |
| `tracingspec.renderer.options.tags-to-display` | Only tags listed here are rendered | `Set<String>` |  |
| `tracingspec.retriever.jaeger.host` | Jaeger Query service host | `String` |  |
| `tracingspec.retriever.jaeger.port` | Jaeger Query service port | `Integer` | `16686` |
| `tracingspec.retriever.jaeger.timeout-millis` | Retrieving timeout in milliseconds | `Long` | `10000` |
| `tracingspec.retriever.zipkin.connect-timeout-millis` | Connect timeout in milliseconds | `Long` | `1000` |
| `tracingspec.retriever.zipkin.read-timeout-millis` | Read timeout in milliseconds | `Long` | `10000` |
| `tracingspec.retriever.zipkin.url` | Zipkin URL (for example: http://localhost:9411/) | `URL` |  |
| `tracingspec.retriever.zipkin.write-timeout-millis` | Write timeout in milliseconds | `Long` | `1000` |
| `tracingspec.spring.description-only-if-debug` | Add SpecSpan description only if B3 Propagation debug flag is set | `Boolean` |  |
| `tracingspec.spring.enabled` | Is TracingSpec integration with Spring enabled? | `Boolean` | `true` |
<!--/ properties -->
