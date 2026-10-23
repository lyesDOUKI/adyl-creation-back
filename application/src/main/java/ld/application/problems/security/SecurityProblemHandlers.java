package ld.application.problems.security;

import jakarta.servlet.http.HttpServletResponse;
import ld.spring.web.lib.ApiProblem;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

@Component
public class SecurityProblemHandlers {

    private final JsonMapper mapper;

    public SecurityProblemHandlers(JsonMapper mapper) {
        this.mapper = mapper;
    }

    public AuthenticationEntryPoint entryPoint() {
        return (_, response, _) ->
                write(response, new SecurityProblem.Unauthenticated(), mapper);
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (_, response, _) ->
                write(response, new SecurityProblem.Forbidden(), mapper);
    }

    private void write(HttpServletResponse response, ApiProblem problem, JsonMapper mapper)
            throws IOException {
        response.setStatus(problem.getStatus());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getOutputStream(), problem);
    }
}