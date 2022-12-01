package fi.espoo.integration.template.routes.idempotent;


import fi.espoo.integration.camelutils.idempotent.JdbcIdempotentRepositoryFactory;
import fi.espoo.integration.template.Application;
import fi.espoo.integration.template.routes.SimpleRouteTest;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.apache.camel.builder.AdviceWith.adviceWith;

@ActiveProfiles("junit")
@CamelSpringBootTest
@SpringBootTest(
        classes = Application.class,
        properties = {
                "camel.springboot.java-routes-include-pattern=fi/espoo/integration/template/routes/idempotent/SimpleIdempotentRoute*",
        })
@ContextConfiguration(initializers = {DatabaseTestHelper.IdempotentDBInitializer.class})
@EnableRouteCoverage
@UseAdviceWith
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SimpleIdempotentRouteTest {
    private static final Logger logger = LoggerFactory.getLogger(SimpleIdempotentRouteTest.class);

    @Autowired
    private CamelContext camelContext;
    @Autowired
    private ProducerTemplate producerTemplate;

    @Value("${idempotent.database.url}")
    private String dbUrl;
    @Value("${idempotent.database.username}")
    private String dbUser;
    @Value("${idempotent.database.password}")
    private String dbPass;

    private static boolean alreadySetup = false;

    @EndpointInject("mock:beginning")
    private MockEndpoint mockBeginning;
    @EndpointInject("mock:idempotent")
    private MockEndpoint mockIdempotent;
    @EndpointInject("mock:end")
    private MockEndpoint mockEnd;

    private JdbcTemplate jdbcTemplate = null;


    @BeforeEach
    public void resetMockEndpoints() {
        MockEndpoint.resetMocks(camelContext);
        MockEndpoint.setAssertPeriod(camelContext, 200); // set global assert period for this test
    }

    @AfterEach
    public void emptyDb() throws SQLException {
        if (jdbcTemplate == null) {
            Driver driver;
            try {
                driver = DriverManager.getDriver(dbUrl);
            } catch (SQLException ex) {
                throw new SQLException("Could not find driver for url [" + dbUrl
                        + "]. Most likely caused by missing database driver dependency.", ex);
            }
            jdbcTemplate = new JdbcTemplate(new SimpleDriverDataSource(driver, dbUrl, dbUser, dbPass));
            jdbcTemplate.afterPropertiesSet();
        }
        jdbcTemplate.update("DELETE FROM integration_template WHERE processorName = 'SimpleIdempotentRoute.timer.header.someHeader'");
    }

    @BeforeEach
    public void setupRoutes() throws Exception {
        if (alreadySetup) {
            return;
        }

        adviceWith(camelContext, "SimpleIdempotentRoute.timer", advice -> {
            advice.replaceFromWith("direct:hitme");
            advice.weaveAddFirst().to(mockBeginning);
            advice.weaveById("SimpleIdempotentRoute.timer.idempotent").after().to(mockIdempotent);
            advice.weaveAddLast().to(mockEnd);
        });

        camelContext.start();
        alreadySetup = true;
    }

    @Test
    public void test() throws InterruptedException {
        mockBeginning.expectedMessageCount(2);
        mockIdempotent.expectedMessageCount(1);
        mockEnd.expectedMessageCount(2);

        producerTemplate.sendBody("direct:hitme", "");
        producerTemplate.sendBody("direct:hitme", "");

        MockEndpoint.assertIsSatisfied(mockBeginning, mockEnd, mockIdempotent);
    }

    @Test
    public void testKeyRemoval() throws InterruptedException {
        mockBeginning.expectedMessageCount(2);
        mockIdempotent.expectedMessageCount(2);
        mockEnd.expectedMessageCount(2);

        producerTemplate.sendBody("direct:hitme", "");
        producerTemplate.sendBody("direct:removeKey", "");
        producerTemplate.sendBody("direct:hitme", "");

        MockEndpoint.assertIsSatisfied(mockBeginning, mockEnd, mockIdempotent);
    }
}
