# core-api

`core-api` contains the shared contracts used by the platform, core starter, and plugins. Keep this module lightweight and stable: changes here can affect every plugin.

## Responsibilities

- Define base domain models and query objects.
- Define shared request and response DTOs.
- Define inbound and outbound domain ports.
- Define PF4J extension points.
- Define resource hook interfaces.
- Provide security, storage, file, migration, i18n, audit, approval, and custom field contracts.

## What Belongs Here

Typical packages:

- `domain/model`: base models, pagination, filters, sorting, lifecycle status, module information.
- `domain/port`: inbound service ports and outbound repository ports.
- `application/dto`: base request and response DTOs.
- `application/exception`: shared application exceptions.
- `application/hook`: resource hook contracts.
- `presentation/dto`: shared API response and search request objects.
- `extension`: generic application extension contracts.
- `migration`: Flyway migration extension point.
- `i18n`: plugin message bundle extension point.
- `security`: authentication and security context abstractions.
- `storage`: file storage extension contracts.
- `file`: file reader contracts and row/input abstractions.
- `approval`, `customfield`, `audit`: cross-cutting capability contracts.

## Hook Contracts

Hook contracts live in `application/hook` because hooks are part of the shared resource extension API.

Current hook families:

- `ResourceControllerHook`: behavior near the REST boundary.
- `ResourceMapperHook`: DTO, model, or entity mapping customization.
- `ResourceRepositoryHook`: persistence and query customization.
- `ResourceServiceHook`: business rules around service operations.
- `ResourceHook`: common base contract.
- `HookLayer`: layer classification for hooks.

Use hook contracts when a plugin needs to extend a standard resource lifecycle without replacing the base flow.

Good hook use cases:

- validate or enrich incoming data before persistence;
- add mapper behavior for a resource;
- constrain repository queries;
- run service-level rules during create, update, delete, or approval flows;
- shape controller responses consistently.

Avoid hooks when the behavior is an independent use case. In that case, prefer a dedicated service or endpoint in the plugin.

## Design Guidelines

- Do not add heavy Spring runtime behavior here.
- Avoid depending on implementation modules.
- Keep interfaces small and explicit.
- Prefer stable, reusable contracts over resource-specific shortcuts.
- Treat breaking changes as platform-wide changes because plugins compile against this module.

## Build

From the repository root:

```bash
mvn -pl core-api -am package
```

Run verification for this module:

```bash
mvn -pl core-api verify
```
