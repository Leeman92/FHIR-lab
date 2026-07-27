package dev.patricklehmann.fhirlab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.UrlHandlerFilter;

@Configuration
public class WebConfiguration {
    @Bean
    UrlHandlerFilter trailingSlashFilter() {
        return UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build();
    }
}
