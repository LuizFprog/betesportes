// config/JacksonConfig.java
package com.luizfprog.betesportes.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public Hibernate6Module hibernate6Module() {
        Hibernate6Module m = new Hibernate6Module();
        m.disable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
        m.enable(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);
        return m;
    }
}
