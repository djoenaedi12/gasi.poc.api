# core-starter

`core-starter` contains reusable Spring implementations used by the platform and plugins. It turns the contracts from `core-api` into common runtime behavior: base controllers, services, repositories, mappers, hook registries, filtering, file reading, encryption helpers, and exception handling.

## Responsibilities

- Provide base controller and service implementations.
- Provide repository adapter and filtering support.
- Provide hook registries and invoke hooks from reusable flows.
- Provide mapper helpers for DTOs, domain models, and entities.
- Provide approval and custom field registries.
- Provide file reader implementations.
- Provide shared infrastructure utilities.
- Provide presentation-level exception and response support.

## Typical Packages

- `presentation/controller`: reusable REST controller base classes.
- `presentation/handler`: global exception handling.
- `presentation/support`: response projection support.
- `application/service`: reusable service implementations.
- `application/mapper`: DTO mapper support.
- `application/hook`: hook registries.
- `application/approval`: approval extension and target registries.
- `application/customfield`: custom field extension integration.
- `application/support`: reference resolution helpers.
- `infrastructure/adapter`: repository adapter base classes.
- `infrastructure/specification`: generic JPA specification support.
- `infrastructure/filter`: filterable field resolution.
- `infrastructure/entity`: base JPA entity.
- `infrastructure/mapper`: entity/domain mapper helpers.
- `infrastructure/file/reader`: file reader implementations.
- `infrastructure/crypto`: field-level encryption support.
- `infrastructure/i18n`: message utility.
- `infrastructure/util`: ID encoding and hashing utilities.

## Hooks In core-starter

`core-starter` should not define hook contracts. Those belong in `core-api`.

This module owns the Spring-side execution model:

- discover hook beans;
- register hooks by layer/resource;
- make base controllers, services, mappers, and repositories call the correct hooks;
- keep hook execution consistent across generated and hand-written resources.

Current registries:

- `ResourceControllerHookRegistry`
- `ResourceMapperHookRegistry`
- `ResourceRepositoryHookRegistry`
- `ResourceServiceHookRegistry`

When adding a new base flow, ask whether it needs a hook point. If it does, define the contract in `core-api` first, then implement discovery and invocation here.

## Usage Pattern

A plugin should use `core-starter` when it wants the standard resource lifecycle:

```text
Controller -> Service -> Repository Adapter -> Entity Repository
     |            |              |
     v            v              v
 controller    service      repository
   hooks        hooks          hooks
```

This keeps common concerns consistent:

- API responses and exception mapping;
- filtering and sorting;
- DTO/domain/entity mapping;
- audit and lifecycle handling;
- hook execution;
- ID encoding;
- reusable file import behavior.

## Design Guidelines

- Keep implementation generic and reusable.
- Prefer extension through `core-api` contracts over direct plugin coupling.
- Keep controller logic thin; use services for business behavior.
- Keep persistence details behind repository adapters.
- Keep mapper behavior deterministic and testable.
- Be careful with hook ordering, transaction boundaries, and error propagation.

## Build

From the repository root:

```bash
mvn -pl core-starter -am package
```

Run verification for this module:

```bash
mvn -pl core-starter verify
```
