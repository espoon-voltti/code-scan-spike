package fi.espoo.integration.template;

import fi.espoo.integration.template.beans.BasicAuthBean;
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

/**
 * Beans that are not implemented in the project are defined here.
 */
@Configuration
public class CamelConfiguration {
    
    @Autowired
    CamelContext camelContext;
    
    @Value("${auto.startup}")
    private Boolean startup;

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelConfiguration.class);


    @Value("${oracle.paas.conversion.username}")
    private String tatuUsername;
    @Value("${oracle.paas.conversion.password}")
    private String tatuPassword;
    @Value("${oracle.erpintegrations.username}")
    private String erpUsername;
    @Value("${oracle.erpintegrations.password}")
    private String erpPassword;
    @Value("${oracle.journalimport.username}")
    private String journalUsername;
    @Value("${oracle.journalimport.password}")
    private String journalPassword;
    @Value("${oracle.paas.username}")
    private String ensioUsername;
    @Value("${oracle.paas.password}")
    private String ensioPassword;


    @Bean(name = "tatuBasicAuth")
    public BasicAuthBean tatuBasicAuth() {
        return new BasicAuthBean(tatuUsername, tatuPassword);
    }

    @Bean(name = "oracleRestBasicAuth")
    public BasicAuthBean oracleRestBasicAuth() {
        return new BasicAuthBean(erpUsername, erpPassword);
    }

    @Bean(name = "ensioBasicAuth")
    public BasicAuthBean ensioBasicAuth() {
        return new BasicAuthBean(ensioUsername, ensioPassword);
    }

    @Bean(name = "journalBasicAuth")
    public BasicAuthBean journalBasicAuth() {
        return new BasicAuthBean(journalUsername, journalPassword);
    }


    /**
     * Additional context configurations.
     * 
     * @return CamelContextConfiguration
     */
    @Bean
    CamelContextConfiguration camelContextConfiguration() {
        
        return new CamelContextConfiguration() {
            
            public void beforeApplicationStart(CamelContext camelContext) {
                camelContext.setAutoStartup(startup);
                camelContext.disableJMX();
            }
            
            public void afterApplicationStart(CamelContext camelContext) {
                
            }
        };
    }
    
    /**
     * UUID generator for routes.
     * 
     * @return UuidGenerator
     */
    @Bean
    public UuidGenerator uuidGenerator() {
        return new DefaultUuidGenerator();
    }
}
