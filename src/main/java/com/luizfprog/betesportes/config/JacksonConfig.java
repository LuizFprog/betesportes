// config/JacksonConfig.java
package com.luizfprog.betesportes.config;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public Hibernate5Module hibernate5Module() {
        Hibernate5Module module = new Hibernate5Module();
        // Não forçar fetching de lazy, e serializar apenas id quando lazy não carregado
        module.disable(Hibernate5Module.Feature.FORCE_LAZY_LOADING);
        module.enable(Hibernate5Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);
        return module;
    }
}
