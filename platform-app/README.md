# platform-app

`platform-app` is the executable Spring Boot host for GASI API. It starts the HTTP server, loads PF4J plugins, wires plugin Spring components, combines plugin migrations and i18n bundles, and exposes platform-level endpoints.

## Responsibilities

- Start the Spring Boot application.
- Load and start PF4J plugin JARs from the runtime `plugins/` directory.
- Build a composite classloader that includes plugin classloaders.
- Register `PluginManager` as a Spring singleton bean.
- Scan platform, core-starter, and plugin components.
- Enable entity scan and JPA repository scan for plugin packages.
- Combine Flyway migration locations from platform and plugins.
- Combine message bundles from platform and plugins.
- Expose platform endpoints such as `/platform/health`.

## Key Files

- `src/main/java/gasi/gps/platform/PlatformApplication.java`: application entry point.
- `src/main/java/gasi/gps/platform/bootstrap/PluginBootstrap.java`: PF4J startup and classloader setup.
- `src/main/java/gasi/gps/platform/bootstrap/DynamicPluginComponentScanner.java`: plugin Spring component registration.
- `src/main/java/gasi/gps/platform/bootstrap/PluginMetadataRegistry.java`: plugin metadata lookup.
- `src/main/java/gasi/gps/platform/infrastructure/classloader/CompositeClassLoader.java`: app + plugin classloader delegation.
- `src/main/java/gasi/gps/platform/infrastructure/config/PluginFlywayConfig.java`: platform and plugin migration wiring.
- `src/main/java/gasi/gps/platform/infrastructure/config/PluginMessageSourceConfig.java`: platform and plugin i18n wiring.
- `src/main/java/gasi/gps/platform/infrastructure/config/PluginStartupLogger.java`: startup summary for loaded plugins.
- `src/main/java/gasi/gps/platform/presentation/controller/PlatformController.java`: platform health endpoint.
- `src/main/resources/application.properties`: default runtime configuration.

## Runtime Configuration

Default settings:

```properties
server.port=8080
server.servlet.context-path=/platform-app
spring.datasource.url=jdbc:mariadb://localhost:3306/demo2
spring.datasource.username=gasi
spring.datasource.password=Very$ecret12
spring.flyway.enabled=true
spring.flyway.table=schema_histories
```

For local development, either create a matching MariaDB database or override the properties for your environment.

Example local setup:

```sql
CREATE DATABASE demo2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'gasi'@'localhost' IDENTIFIED BY 'Very$ecret12';
GRANT ALL PRIVILEGES ON demo2.* TO 'gasi'@'localhost';
FLUSH PRIVILEGES;
```

Recommended environment overrides:

```bash
export AUTH_JWT_SECRET="replace-with-a-strong-base64-secret"
export APP_FIELD_ENCRYPTION_ENABLED=false
export APP_FIELD_ENCRYPTION_KEY=""
```

If field-level encryption is enabled, provide a Base64 AES key:

```bash
openssl rand -base64 32
```

## Run

From the repository root:

```bash
mvn -pl platform-app -am spring-boot:run
```

Default base URL:

```text
http://localhost:8080/platform-app
```

Health endpoint:

```text
GET http://localhost:8080/platform-app/platform/health
```

## Runtime Plugins

PF4J reads plugins from a `plugins/` directory relative to the Java process working directory.

That means:

- If the app is started from the repository root, the runtime plugin directory is `gasi.poc.api/plugins/`.
- If the app is started from `platform-app`, the runtime plugin directory is `gasi.poc.api/platform-app/plugins/`.

Example using `platform-app/plugins` as the runtime directory:

```bash
mvn -pl plugins/data-upload-plugin -am package
mkdir -p platform-app/plugins
cp plugins/data-upload-plugin/target/data-upload-plugin-1.0.0.jar platform-app/plugins/
cd platform-app
mvn spring-boot:run
```

Startup sequence:

1. Create a `DefaultPluginManager`.
2. Load and start plugin JARs from `plugins/`.
3. Collect started plugin classloaders.
4. Build a composite classloader.
5. Start Spring Boot with the composite classloader.
6. Register `PluginManager` in the Spring context.

## Flyway And I18n

Flyway uses:

```properties
spring.flyway.table=schema_histories
```

Plugins can contribute migration locations through `FlywayMigrationExtension`. Use timestamp-based migration versions to keep versions unique across platform and plugins:

```text
V<YYYYMMDDHHmmss>__<description>.sql
```

Platform message bundles:

```text
src/main/resources/messages.properties
src/main/resources/messages_id.properties
```

Plugins can contribute additional bundles through `I18nExtension`.

## Troubleshooting

If the app starts but a plugin does not appear in `/platform/health`:

- Make sure the plugin JAR is in the `plugins/` directory relative to the working directory.
- Make sure the plugin manifest contains `Plugin-Id`, `Plugin-Class`, and `Plugin-Version`.
- Check startup logs from `PluginStartupLogger`.

If Flyway fails:

- Make sure the database is reachable with the configured credentials.
- Make sure migration versions are unique across platform and plugin migrations.
- Check whether the plugin migration location is collected by `PluginFlywayConfig`.

If plugin Spring components are not scanned:

- Make sure the plugin is started by PF4J.
- Make sure the plugin package is under a package root scanned by the platform.
- Check `DynamicPluginComponentScanner` and plugin metadata.

## Build

From the repository root:

```bash
mvn -pl platform-app -am package
```

Run tests:

```bash
mvn -pl platform-app test
```
