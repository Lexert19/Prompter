package com.example.promptengineering.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;

import com.example.promptengineering.converter.HashMapToJsonConverter;
import com.example.promptengineering.converter.JsonToHashMapConverter;

import io.r2dbc.spi.ConnectionFactory;

@Configuration
public class R2dbcConfig extends AbstractR2dbcConfiguration {
    
    @Override
    protected List<Object> getCustomConverters() {
        return Arrays.asList(
            new HashMapToJsonConverter(),
            new JsonToHashMapConverter()  
        );
    }

    @Override
    public ConnectionFactory connectionFactory() {
        throw new UnsupportedOperationException("Unimplemented method 'connectionFactory'");
    }
}