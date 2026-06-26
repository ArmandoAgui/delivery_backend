# Architecture Migration Report

## Branch

`feature/n-layer-architecture`

## Goal

Migrate the backend package structure from domain/module packages to a classic N-layer architecture while preserving existing behavior, endpoints, entities, migrations, and business logic.

## Final Package Layout

All application classes now live under logical layers:

- `sv.edu.uca.delivery.backend.controller`
- `sv.edu.uca.delivery.backend.service`
- `sv.edu.uca.delivery.backend.repository`
- `sv.edu.uca.delivery.backend.entity`
- `sv.edu.uca.delivery.backend.dto`
- `sv.edu.uca.delivery.backend.mapper`
- `sv.edu.uca.delivery.backend.exception`
- `sv.edu.uca.delivery.backend.config`
- `sv.edu.uca.delivery.backend.security`
- `sv.edu.uca.delivery.backend.util`

The application root class remains in:

- `sv.edu.uca.delivery.backend`

## What Changed

- Controllers from every module were moved into the single `controller` layer.
- Services and service implementations were moved into the single `service` layer.
- Repositories were moved into the single `repository` layer.
- JPA entities and enums were moved into the single `entity` layer.
- DTOs, request records, response records, and report projections were moved into the single `dto` layer.
- Mappers were moved into the single `mapper` layer.
- Domain and global exceptions were moved into the single `exception` layer.
- App, OpenAPI, upload, database, and resource configuration classes were moved into the single `config` layer.
- Security classes were flattened into the `security` layer.
- Common pagination/time utilities and UUID helpers were moved into the `util` layer.
- Empty `.gitkeep` directories from the previous domain-module structure were removed.
- Test packages and paths were aligned with the new layered packages.

## What Did Not Change

- REST endpoint paths.
- Database schema or Flyway migration contents.
- Business rules.
- DTO class names and public API contracts.
- Security behavior.
- Docker/configuration files.

## Validation

Executed:

```bash
./mvnw test
```

Result:

- `47` tests passed.
- Build successful.

## Notes

This is intentionally a structural migration. The goal is to make the codebase read as an N-layer Spring Boot application without introducing feature changes or extra refactors during the move.
