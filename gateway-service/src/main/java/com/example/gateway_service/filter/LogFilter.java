package com.example.gateway_service.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;

@Slf4j
@Component
public class LogFilter {

    public ServerRequest filter(ServerRequest request) {
        log.info("Request: {}", request.uri());
        log.info(request.pathVariables().toString());
        return request;
    }
}
