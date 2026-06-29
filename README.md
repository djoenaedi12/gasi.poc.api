# GASI API

GASI API is a modular backend built with Spring Boot and PF4J. The platform is organized around a host application, shared core modules, and feature plugins that can be packaged and loaded as PF4J plugin JARs.

Use this README as the entry point. Module-specific details live closer to the code:

- [core-api](core-api/README.md): shared contracts, DTOs, ports, extension points, and hook interfaces.
- [core-starter](core-starter/README.md): reusable Spring implementations, base CRUD flow, registries, filtering, mapping, and hook execution.
- [platform-app](platform-app/README.md): executable host application, plugin loading, runtime configuration, Flyway, i18n, and troubleshooting.

## Tech Stack

| Area | Technology |
| --- | --- |
| Language | Java 25 |
| Framework | Spring Boot 4.0.3 |
| Build | Maven multi-module |
| Plugin runtime | PF4J 3.15.0, PF4J Spring 0.10.0 |
| Database | MariaDB |
| Migration | Flyway |
| Persistence | Spring Data JPA |
| Mapper | MapStruct 1.6.3 |
| Boilerplate | Lombok |
| Cache | Spring Cache + Caffeine |
| Quality checks | JaCoCo, Checkstyle, SpotBugs, Javadoc |

## Example Project Layout

This layout is illustrative. The actual repository may evolve as modules and plugins are added, renamed, generated, or moved.

```text
gasi.poc.api/
├── pom.xml
├── core-api/
├── core-starter/
├── platform-app/
├── plugins/
│   ├── sample-domain-plugin/
│   └── ...
├── checkstyle.xml
├── checkstyle-suppressions.xml
└── spotbugs-exclude.xml
```

The parent `pom.xml` is the source of truth for modules included in the default Maven build. Check the `<modules>` section before assuming a plugin is built by `mvn clean package`.

## Runtime Flow

```text
client
  |
  v
platform-app
  |-- starts Spring Boot
  |-- loads PF4J plugin jars
  |-- scans platform, core-starter, and plugin components
  |-- combines plugin Flyway migrations and i18n bundles
  v
plugin domain modules
```

`core-api` defines the contracts. `core-starter` provides reusable Spring behavior. `platform-app` runs the application and wires plugin contributions at runtime.

## Prerequisites

- JDK 25
- Maven 3.9+
- MariaDB

Quick check:

```bash
java --version
mvn --version
```

## Build

From the repository root:

```bash
mvn clean package
```

Run tests and lifecycle quality checks:

```bash
mvn clean verify
```

Build one module with its required internal dependencies:

```bash
mvn -pl platform-app -am package
mvn -pl plugins/data-upload-plugin -am package
```

## Run

Review [platform-app/README.md](platform-app/README.md) for runtime configuration first, especially database settings.

Then run:

```bash
mvn -pl platform-app -am spring-boot:run
```

Default base URL:

```text
http://localhost:8080/platform-app
```

Platform health endpoint:

```text
GET http://localhost:8080/platform-app/platform/health
```

## New Developer Reading Path

1. Read this root README.
2. Read [core-api](core-api/README.md) to understand contracts and extension points.
3. Read [core-starter](core-starter/README.md) to understand reusable base behavior.
4. Read [platform-app](platform-app/README.md) to understand startup and runtime wiring.
5. Read one plugin module end to end to see the domain pattern in practice.
