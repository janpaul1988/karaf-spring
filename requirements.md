# Requirements: Karaf Runtime with Embedded Spring Boot API

## Overview

This project runs an Apache Karaf 4.4.11 OSGi container with a Spring Boot 3.4.5 REST API embedded inside it as an OSGi bundle. The purpose is to validate that Spring Boot can be bootstrapped from within an OSGi `BundleActivator` and that inter-bundle dependencies resolve correctly across a multi-level Karaf feature hierarchy.

---

## System Architecture

```
Apache Karaf 4.4.11 (OSGi container)
└── feature4 (installed at startup)
    ├── feature2 → com.example.osgi.bundle2 (v1.1)
    │              └── [provided by bundle4 classloader]
    └── feature3 → feature1 → com.example.osgi.bundle1 (v1.1)
                └── com.example.osgi.bundle3 (v1.1)
                               └── [provided by bundle4 classloader]

com.example.osgi.bundle4 (v1.1) — Spring Boot bundle
├── BundleActivator: SpringBootBundleActivator
├── Embedded: Spring Boot 3.4.5 + Jetty 12 (all compile deps embedded via maven-bundle-plugin)
└── REST: GET /hello → port 8081
```

### Bundle roles

| Bundle | Role |
|--------|------|
| `bundle1` | Base greeting; `hello()` returns `"hello from bundle1"` |
| `bundle2` | Simple greeting; `hello()` returns `"hello from bundle2"` |
| `bundle3` | Delegates to bundle1; `hello()` returns `"hello from bundle1called from bundle 3"` |
| `bundle4` | Spring Boot REST API; `BundleActivator` starts an embedded Jetty on port 8081 |

### Feature repository hierarchy

Feature definitions span three nested repositories:

- `feature-repo-1` (v1.1): defines `feature1` (bundle1) and `feature2` (bundle2)
- `feature-repo-2` (v1.1): defines `feature3` (bundle3 + feature1 dependency), imports feature-repo-1
- `feature-repo-3` (v1.1): defines `feature4` (bundle4 + feature2/feature3 dependencies), imports both repos above

This three-level transitive hierarchy is the core complexity under test.

---

## Spring Boot Bootstrap Mechanism

`SpringBootBundleActivator` implements both `BundleActivator` and carries `@SpringBootApplication`.

On `start()`:
1. The thread context classloader is set to the bundle's own classloader — required so Spring Boot's classpath scanning finds embedded classes rather than the OSGi system classloader.
2. `SpringApplication.run()` starts the embedded Jetty server.

On `stop()`:
1. `SpringApplication.exit()` shuts down the application context.

The bundle JAR embeds all compile-scope transitive dependencies (Spring Boot 3.x, Jetty 12 EE10, Jackson, etc.) using `maven-bundle-plugin` `Embed-Dependency` + `Embed-Transitive`. This is the only viable embedding strategy because Spring Framework 6.x JARs in Maven Central no longer carry OSGi manifest headers and cannot be installed as first-class OSGi bundles.

Key manifest headers for Spring Boot 3.x compatibility:
- `!org.springframework.*` and `!jakarta.*` excluded from `Import-Package` (all embedded)
- `DynamicImport-Package: *` required because CGLIB and Spring AOP generate proxy classes at runtime that cannot be declared statically in the manifest
- All other imports are `resolution:=optional`

Spring Boot 3.x startup is started on a **daemon background thread** rather than blocking `BundleActivator.start()`. This prevents Karaf from timing out bundle activation while Jetty 12 + Spring context initialisation completes.

---

## REST Endpoint Specification

| Property | Value |
|----------|-------|
| Port | 8081 |
| Path | `/hello` |
| Method | GET |
| Content-Type | `text/plain` |
| Expected body | `hello from bundle2hello from bundle1called from bundle 3` |

The response is the concatenation of `Bundle2.hello()` and `Bundle3.hello()`, assembled in `HelloController.getMessage()`.

---

## Integration Test Requirements

### REQ-01 — Karaf Startup

The Karaf runtime must start from the assembled distribution at `karaf-assembly/target/assembly` without errors. All bundles in the `feature4` dependency chain must reach ACTIVE state.

### REQ-02 — Spring Boot Server Availability

Within **3 minutes** of Karaf startup, the embedded Jetty server must accept TCP connections on port 8081.

### REQ-03 — HTTP 200 Response

`GET http://localhost:8081/hello` must return HTTP status 200.

### REQ-04 — Correct Response Body

The response body must be exactly:

```
hello from bundle2hello from bundle1called from bundle 3
```

This verifies:
- `bundle2` is accessible from `bundle4`'s embedded classloader
- `bundle3` is accessible from `bundle4`'s embedded classloader
- `bundle1` (transitive via `bundle3`) resolves correctly across the feature hierarchy

---

## Running the Integration Tests

The integration tests live in the `integration-test` Maven module and run via Maven Failsafe during the `verify` phase.

**Full build including integration tests:**
```bash
cd karaf-plugin-bug
mvn clean install
```

**Integration tests only (assembly must already be built):**
```bash
mvn verify -pl integration-test
```

**Build everything except integration tests:**
```bash
mvn install -pl '!integration-test'
```

The `karaf.assembly.dir` system property is injected by Failsafe and points to `karaf-assembly/target/assembly`. The test starts Karaf as a subprocess, polls `/hello` until it responds, runs the assertions, then shuts Karaf down.
