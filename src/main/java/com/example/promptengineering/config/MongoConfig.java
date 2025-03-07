package com.example.promptengineering.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
public class MongoConfig extends AbstractReactiveMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "prompter";
    }

    @Override
    public MongoClient reactiveMongoClient() {
        return MongoClients.create();
    }

    @Bean
    public MappingMongoConverter mappingMongoConverter() throws Exception {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver((MongoDatabaseFactory) reactiveMongoDbFactory());
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext(null, null));
        converter.setCustomConversions(customConversions());
        return converter;
    }
}
