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

## Application

Download [tracing-spec-app.jar](#) and use it as a general Spring Boot application.

### Configuration

The application should be configured using [Spring Boot Externalized Configuration feature](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config).

Spring properties a listed [below](#spring-properties).

### Usage

#### Render

Command: `java -jar render <traceId> <rendererName> <outputPath>`
* `traceId` - Trace ID
* `rendererName` - Renderer name
* `outputPath` - Output file path

Current renderer names:
* `plantuml-sequence` - [plantuml sequence diagram](https://plantuml.com/sequence-diagram)
* `json` - JSON
* `yaml` - YAML

#### Match

Command: `java -jar match <traceId> <patternGraphFile>`
* `traceId` - Trace ID
* `patternGraphFile` - Pattern graph file (YAML/JSON/JSON5)
* Optional parameters:
  * `--attempts=<number>` - Number of attempts to retrieve spans and match them
  * `--attempts-delay=<number>` - Delay between attempts, in milliseconds

### Spring properties

#### Distributed tracing spans retrieving properties

<!-- properties: tracingspec.retriever -->
`tracingspec.retriever.jaeger.host`<br>
Jaeger Query service host<br>
Type: `String`

`tracingspec.retriever.jaeger.port`<br>
Jaeger Query service port<br>
Type: `Integer`<br>
Default value: `16686`

`tracingspec.retriever.jaeger.timeout-millis`<br>
Retrieving timeout in milliseconds<br>
Type: `Long`<br>
Default value: `10000`

`tracingspec.retriever.zipkin.connect-timeout-millis`<br>
Connect timeout in milliseconds<br>
Type: `Long`<br>
Default value: `1000`

`tracingspec.retriever.zipkin.read-timeout-millis`<br>
Read timeout in milliseconds<br>
Type: `Long`<br>
Default value: `10000`

`tracingspec.retriever.zipkin.url`<br>
Zipkin URL (for example: http://localhost:9411/)<br>
Type: `URL`

`tracingspec.retriever.zipkin.write-timeout-millis`<br>
Write timeout in milliseconds<br>
Type: `Long`<br>
Default value: `1000`

<!--/ properties -->

#### Rendering properties

<!-- properties: tracingspec.renderer -->
`tracingspec.renderer.options.graph-processors`<br>
SpecSpansGraph processors<br>
Type: `List<SpecSpansGraphProcessor>`

`tracingspec.renderer.options.node-processors`<br>
SpecSpanNode processors<br>
Type: `List<SpecSpanNodeProcessor>`

`tracingspec.renderer.options.tags-to-display`<br>
Only tags listed here are rendered<br>
Type: `Set<String>`

<!--/ properties -->
