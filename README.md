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

<img align="left" width="100" height="100" src="https://raw.githubusercontent.com/remal/tracing-spec/master/logo.svg" alt="logo"/>

Sometimes you have really complex business scenarios with a lot of different services involved. Especially in microservices architecture. Just imagine: you have a scenario where the logic is distributes among several (micro)services with different communication styles - some services interact via REST HTTP, another - via some messaging system (like Kafka). Also, these services publish different notifications that can be consumed by some external services.

How to document it? How to make it obvious to QA team or newcomers?

Yes, you can write documentation manually, but there are some problems here:
1. The documentation should be maintained, which is hard in active development stage of the project
1. How to validate it?

This projects helps to solve such problems by handling data from distributed tracing system (e.g. [Zipkin](https://zipkin.io/), [Jaeger](https://www.jaegertracing.io/)) and providing visualization and validation functionality

<br clear="left"/>

<!--/ description ->>

2
