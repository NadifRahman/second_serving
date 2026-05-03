package com.secondserving.secondserving.config;

import com.secondserving.secondserving.controller.AuthController;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

/**
 * Springdoc/OpenAPI configuration for the generated API contract and Swagger UI.
 * <p>
 * Springdoc can infer many successful responses from controller method return types, but it cannot reliably infer
 * endpoint error responses that are produced indirectly through {@link com.secondserving.secondserving.controller.GlobalExceptionHandler}.
 * The {@link #globalExceptionResponses()} customizer fills in those shared error responses so Swagger UI and generated
 * TypeScript types show the same response shapes that clients can receive at runtime.
 * 
 */
@Configuration
public class OpenApiConfig {

    /**
     * Name of the JWT Bearer security scheme used in the OpenAPI document.
     */
    public static final String BEARER_AUTH_SCHEME = "bearerAuth";

    /**
     * Builds the root OpenAPI document metadata and declares JWT Bearer authentication.
     * <p>
     * Adding the security scheme here makes Swagger UI show the "Authorize" button. After a developer pastes a JWT
     * there, Swagger UI can send authenticated requests to protected endpoints.
     *
     * @return the base OpenAPI model used by springdoc
     */
    @Bean
    OpenAPI secondServingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SecondServing API")
                        .version("v1")
                        .description("API contract for the SecondServing backend."))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME));
    }

    /**
     * Adds common error responses to each documented controller operation.
     * <p>
     * This is intentionally broad. The backend's global exception handler maps domain and validation exceptions to
     * plain-text error responses, but those mappings are not visible from a controller method signature alone. This
     * customizer documents those possible responses once instead of repeating the same OpenAPI annotations on every
     * endpoint.
     *
     * @return a springdoc customizer that runs for every discovered controller operation
     */
    @Bean
    OperationCustomizer globalExceptionResponses() {
        return (operation, handlerMethod) -> {
            ApiResponses responses = operation.getResponses();

            // Setup failure responses for the different end point handler methods for OpenAPI here.

            addTextResponse(responses, "400", "Bad request or validation failure.");

            if (isLoginEndpoint(handlerMethod)) {
                addTextResponse(responses, "401", "Invalid username or password.");
            }

            if (!isAuthEndpoint(handlerMethod)) {
                addTextResponse(responses, "401", "Missing or invalid JWT.");
                addTextResponse(responses, "403", "Authenticated user is not allowed to perform this action.");
                addTextResponse(responses, "404", "Requested item was not found.");
                addTextResponse(responses, "409", "Request conflicts with the current state of the resource.");
            }

            return operation;
        };
    }

    /**
     * Determines whether an operation belongs to the public authentication controller.
     * <p>
     * Auth endpoints do not require an existing JWT, so they should not be documented as returning missing-token
     * responses.
     *
     * @param handlerMethod the Spring MVC handler method behind the OpenAPI operation
     * @return {@code true} when the operation is declared by {@link AuthController}
     */
    private static boolean isAuthEndpoint(HandlerMethod handlerMethod) {
        return handlerMethod.getBeanType().equals(AuthController.class);
    }

    /**
     * Determines whether an operation is the login endpoint.
     *
     * @param handlerMethod the Spring MVC handler method behind the OpenAPI operation
     * @return {@code true} when the operation is {@link AuthController#login(com.secondserving.secondserving.dto.LoginRequestDto)}
     */
    private static boolean isLoginEndpoint(HandlerMethod handlerMethod) {
        return isAuthEndpoint(handlerMethod) && handlerMethod.getMethod().getName().equals("login");
    }

    /**
     * Adds a plain-text response to an operation if the controller or another customizer has not already declared it.
     *
     * @param responses the operation response map to update
     * @param statusCode the HTTP status code to document
     * @param description the human-readable Swagger UI description for the response
     */
    private static void addTextResponse(ApiResponses responses, String statusCode, String description) {
        if (responses.containsKey(statusCode)) {
            return;
        }

        responses.addApiResponse(statusCode, new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(org.springframework.http.MediaType.TEXT_PLAIN_VALUE,
                        new MediaType().schema(new StringSchema()))));
    }
}
