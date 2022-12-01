package org.test.code_scan_spike;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.UuidGenerator;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.apache.camel.support.DefaultUuidGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CamelConfiguration.class);
    
    @Autowired
    CamelContext camelContext;
    
    @Value("${auto.startup}")
    private Boolean startup;

    @Bean
    CamelContextConfiguration camelContextConfiguration() {
        
        return new CamelContextConfiguration() {
            
            public void beforeApplicationStart(CamelContext camelContext) {
                camelContext.setAutoStartup(startup);
                camelContext.disableJMX();
                camelContext.setStreamCaching(false);
            }
            
            public void afterApplicationStart(CamelContext camelContext) {
                
            }
        };
    }
    
    @Bean
    public UuidGenerator uuidGenerator() {
        return new DefaultUuidGenerator();
    }
}
