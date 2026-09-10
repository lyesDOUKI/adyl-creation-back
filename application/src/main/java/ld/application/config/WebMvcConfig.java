package ld.application.config;

import ld.application.infra.security.CurrentCustomerIdResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CurrentCustomerIdResolver currentCustomerIdResolver;

    public WebMvcConfig(CurrentCustomerIdResolver currentCustomerIdResolver) {
        this.currentCustomerIdResolver = currentCustomerIdResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentCustomerIdResolver);
    }
}