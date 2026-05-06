# OpenAPI & Swagger Docs

CartWave exposes runtime API documentation through Springdoc.

## Interactive Swagger UI

- URL: `http://localhost:8080/swagger-ui/index.html`

## Raw OpenAPI Spec

- JSON URL: `http://localhost:8080/api-docs`

## Generate Static API Docs

Use the helper script to export the OpenAPI spec into the repository docs folder:

```bash
./scripts/generate-openapi.sh
```

This generates:

- `docs/openapi.json`
- `docs/openapi.yaml` (when `yq` or `PyYAML` is available)

## Notes

- The export uses the `local` Spring profile.
- If generation fails, inspect `target/openapi-generation.log`.
- Keep `docs/API.md` and exported OpenAPI files in sync when endpoints change.
