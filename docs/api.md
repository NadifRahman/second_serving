# API Documentation And Types

The backend publishes an OpenAPI document with springdoc-openapi. When running the app on localhost:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

OpenAPI is the machine-readable contract for the REST API. Swagger UI is the browser page that renders that contract so you can inspect endpoints, request bodies, responses, and schemas. The schemas come from the Java controller signatures and DTO records, including Jakarta validation annotations like `@NotNull`, `@Size`, and `@Min`.

## Auth Cookie Contract

Login and signup set the JWT in an HttpOnly cookie instead of returning the raw token in the JSON response. The frontend should send browser-managed cookies with requests, then use `/api/users/me` as the source of truth for the currently authenticated user.

JWT expiration and cookie expiration are separate mechanisms:

- The JWT expiration claim controls whether the backend will accept the token.
- The cookie expiration controls how long the browser keeps sending the token.

We intentionally set both from the same `app.jwt.expiration-ms` value. That keeps the browser-held cookie and the JWT inside it aligned: around the time the backend would reject the JWT, the browser should also stop sending the cookie.

The frontend can generate TypeScript types from the live backend schema:

```bash
scripts/generate-api-types.sh
```

The Spring Boot backend must already be running when you run that command.

This writes `frontend/src/api/schema.ts`. Typical usage:

```ts
import type { components, paths } from "./api/schema";

type FoodListing = components["schemas"]["FoodListingDto"];
type CreateFoodListingRequest =
  components["schemas"]["CreateFoodListingRequestDto"];
type MyListingsResponse =
  paths["/api/users/me/food-listings"]["get"]["responses"][200]["content"]["application/json"];
```

When a DTO or controller response changes, restart the backend if needed and rerun `scripts/generate-api-types.sh`.

The script defaults to `http://localhost:8080/v3/api-docs`, because type generation is a development-time workflow. You can override that if needed:

```bash
API_SCHEMA_URL=http://localhost:9090/v3/api-docs scripts/generate-api-types.sh
```
