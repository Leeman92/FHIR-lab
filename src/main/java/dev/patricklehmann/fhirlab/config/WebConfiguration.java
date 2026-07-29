package dev.patricklehmann.fhirlab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.UrlHandlerFilter;

/** Web-layer infrastructure that applies across all endpoints. */
@Configuration
public class WebConfiguration {
    @Bean
    /**
     * Treats {@code /patients/} as {@code /patients}, so a trailing slash is not the difference
     * between a hit and a 404.
     */
    UrlHandlerFilter trailingSlashFilter() {
        return UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build();
    }
}
