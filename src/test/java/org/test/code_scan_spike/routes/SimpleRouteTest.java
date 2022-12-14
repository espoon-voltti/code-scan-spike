package org.test.code_scan_spike.routes;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.test.code_scan_spike.Application;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("junit")
@CamelSpringBootTest
@SpringBootTest(
        classes = Application.class,
        properties = {
                "camel.springboot.java-routes-include-pattern=org/test/code_scan_spike/routes/SimpleRoute*"
        })
@EnableRouteCoverage
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SimpleRouteTest {

    private static final Logger logger = LoggerFactory.getLogger(SimpleRouteTest.class);

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;
    
    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
            .options(wireMockConfig().port(8989))
            .configureStaticDsl(true)
            .build();

    private static boolean alreadySetup = false;

    @BeforeEach
    public void setupRoutes() throws Exception {
        wiremock.resetAll();
        MockEndpoint.resetMocks(camelContext);
        
        if (alreadySetup) {
            return;
        }

        adviceWith(camelContext, "SimpleRoute.timer", advice -> {
            advice.replaceFromWith("direct:hitme");
            advice.weaveByToString("To[direct:setProperties]").replace().to("mock:sub");
            advice.weaveByToString("To[direct:process]").replace().to("mock:sub");
            advice.weaveByToString("To[direct:split]").replace().to("mock:sub");
        });

        adviceWith(camelContext, "SimpleRoute.setProperties", advice -> {
            advice.weaveAddLast().to("mock:check");
        });

        adviceWith(camelContext, "SimpleRoute.process", advice -> {
            advice.weaveAddLast().to("mock:check");
        });

        adviceWith(camelContext, "SimpleRoute.split", advice -> {
            advice.weaveById("SimpleRoute.split.lastlog").after().to("mock:splitEnd");
            advice.weaveAddLast().to("mock:end");
        });

        camelContext.start();
        alreadySetup = true;
    }

    /**
     * Write test cases for each routes
     * Keep tests simple by mocking any calls to outside route
     */
    @Test
    public void testSimpleRouteTimer() throws Exception {
        //ensure that all direct routes are called
        MockEndpoint mockOut = camelContext.getEndpoint("mock:sub", MockEndpoint.class);
        mockOut.expectedMessageCount(3);
        producerTemplate.sendBody("direct:hitme", "");
        mockOut.assertIsSatisfied();
    }

    @Test
    public void testSetProperties() throws Exception {
        //test that route creates header and property correctly
        MockEndpoint resultEndpoint = camelContext.getEndpoint("mock:check", MockEndpoint.class);
        resultEndpoint.expectedPropertyReceived("source", "source");
        resultEndpoint.expectedPropertyReceived("destination", "destination");
        producerTemplate.sendBody("direct:setProperties", "[{ \"first_name\": \"Matti\" }]");
        Exchange exchange = resultEndpoint.getExchanges().get(0);
        assertNotNull(exchange.getProperty("source"));
        resultEndpoint.assertIsSatisfied();
    }

    @Test
    public void testProcess() throws Exception {
        stubFor(get(urlPathEqualTo("/hello"))
                .withBasicAuth("tyranno", "saurus")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("World!")
                )
        );
        //ensure that route create a correct body
        MockEndpoint resultEndpoint = camelContext.getEndpoint("mock:check", MockEndpoint.class);
        resultEndpoint.expectedBodiesReceived("hello,world");
        producerTemplate.sendBody("direct:process", "[{ \"first_name\": \"Matti\" }]");
        resultEndpoint.assertIsSatisfied();
    }

    @Test
    public void testSplit() throws Exception {
        MockEndpoint resultEndpoint = camelContext.getEndpoint("mock:end", MockEndpoint.class);
        resultEndpoint.expectedMessageCount(1);
        resultEndpoint.expectedPropertyReceived("logId", "54321");
        //check that split works as expected
        MockEndpoint mockSplitEnd = camelContext.getEndpoint("mock:splitEnd", MockEndpoint.class);
        mockSplitEnd.expectedMessageCount(2);
        mockSplitEnd.expectedBodiesReceived("sentence", " to split");
        producerTemplate.sendBodyAndProperty("direct:split", "sentence, to split", "logId", "54321");
        resultEndpoint.assertIsSatisfied();
        mockSplitEnd.assertIsSatisfied();
    }
}
