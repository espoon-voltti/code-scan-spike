package fi.espoo.integration.template;

import fi.espoo.integration.ssm.config.AwsSsmPropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;

public class AWSPropertyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AWSPropertyInitializer.class);

    /**
     * Initialize AWS SSM parameter store as property source.
     *
     * @param configurableApplicationContext
     */
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        ConfigurableEnvironment environment = configurableApplicationContext.getEnvironment();

        String awsRegion = environment.getProperty("aws.region");

        SsmClient awsSSM = SsmClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(awsRegion))
                .build();
        AwsSsmPropertySource source = new AwsSsmPropertySource("AwsSsmParameterStorePropertySource", awsSSM);
        environment.getPropertySources().addFirst(source);

        LOGGER.info("Configured AwsParameterStorePropertySource for resolving properties from "
                + "SSM parameter store in region " + awsRegion);
    }
}
