package app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.Set;

@TestConfiguration
@ComponentScan("app.web.converters")
public class TestConfig {
    @Autowired
    private Set<Converter<?, ?>> converters;

    @Bean
    @Primary
    public ConversionService conversionService() {
        DefaultConversionService service = new DefaultConversionService();
        converters.forEach(service::addConverter);
        return service;
    }
}
