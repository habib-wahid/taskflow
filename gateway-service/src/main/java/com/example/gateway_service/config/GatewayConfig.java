package com.example.gateway_service.config;

import com.example.gateway_service.filter.AuthenticationFilter;
import com.example.gateway_service.filter.LogFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Slf4j
@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authenticationFilter;
    private final LogFilter logFilter;

    public GatewayConfig(AuthenticationFilter authenticationFilter, LogFilter logFilter) {
        this.authenticationFilter = authenticationFilter;
        this.logFilter = logFilter;
    }

    @Bean
    public RouterFunction<ServerResponse> authServiceRoutes() {
        System.out.println("HERE");
        log.info("Auth service routes configured");
        return GatewayRouterFunctions.route("auth-service")
                .route(request -> request.path().startsWith("/api/auth"), HandlerFunctions.http())
                .before(BeforeFilterFunctions.uri("http://localhost:8081"))
                .before(logFilter::filter)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> projectServiceWorkspacesRoutes() {
        return GatewayRouterFunctions.route("project-service-workspaces")
                .route(request -> request.path().startsWith("/api/workspaces"), HandlerFunctions.http())
                .before(BeforeFilterFunctions.uri("http://localhost:8082"))
                .before(authenticationFilter::filter)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> projectServiceProjectsRoutes() {
        return GatewayRouterFunctions.route("project-service-projects")
                .route(request -> request.path().startsWith("/api/projects"), HandlerFunctions.http())
                .before(BeforeFilterFunctions.uri("http://localhost:8082"))
                .before(authenticationFilter::filter)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> healthRoutes() {
        return GatewayRouterFunctions.route("health")
                .GET("/api/health", HandlerFunctions.http())
                .before(BeforeFilterFunctions.uri("http://localhost:8082"))
                .build();
    }
}
