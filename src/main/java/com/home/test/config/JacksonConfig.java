package com.home.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Double.class, new JsonSerializer<Double>() {
            @Override
            public void serialize(Double value, JsonGenerator gen, com.fasterxml.jackson.databind.SerializerProvider provider) throws IOException {
                // Format double to avoid scientific notation for very small numbers
                if (value != null && value < 0.001 && value > 0) {
                    gen.writeRawValue(String.format("%.8f", value));
                } else {
                    gen.writeNumber(value);
                }
            }
        });

        return new Jackson2ObjectMapperBuilder()
                .modules(module)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .indentOutput(false);
    }
}