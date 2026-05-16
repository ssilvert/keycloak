# Keycloak Codebase Context

## Architecture

- SPI-based extension model: implementations go in `services/`, interfaces in `core/`
- Quarkus-based server, built with pnpm monorepo for the admin console (React/TypeScript)
- Realm isolation is a hard boundary — cross-realm data access is always a bug

## Security-critical areas

- `services/src/main/java/org/keycloak/authorization/` — FGAP/AuthZ logic
- Admin REST API endpoints — check for proper permission checks on every operation
- Token issuance and validation paths

## Review conventions

- New SPIs must follow the existing provider factory pattern
- Admin REST resources must check permissions via AdminPermissions, not raw role checks
- No business logic in JAX-RS resource classes — delegate to managers/services
- JavaScript source map files (.map) must never be served in production

## What to flag

- Cross-realm data leakage
- Missing permission checks on admin operations
- SPI implementations that bypass the provider registry
- Hardcoded realm or client names
