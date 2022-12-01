package org.test.code_scan_spike.routes.multicast;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.test.code_scan_spike.Application;

import static org.apache.camel.builder.AdviceWith.adviceWith;

@ActiveProfiles("junit")
@CamelSpringBootTest
@SpringBootTest(classes = Application.class, properties = {"camel.springboot.java-routes-include-pattern=**/Multicast*"})
@EnableRouteCoverage
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MulticastMainRouteTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MulticastMainRouteTest.class);

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    @EndpointInject("mock:subAInput")
    private MockEndpoint mockSubAInput;

    @EndpointInject("mock:subBInput")
    private MockEndpoint mockSubBInput;

    @EndpointInject("mock:subADone")
    private MockEndpoint mockSubADone;

    @EndpointInject("mock:subBDone")
    private MockEndpoint mockSubBDone;

    @EndpointInject("mock:mainDone")
    private MockEndpoint mockMainDone;

    @EndpointInject("mock:mainBeforeMulticast")
    private MockEndpoint mockMainBeforeMulticast;

    @EndpointInject("mock:mainAfterMulticast")
    private MockEndpoint mockMainAfterMulticast;

    @EndpointInject("mock:subASetBody")
    private MockEndpoint mockSubASetBody;

    @EndpointInject("mock:subBSetBody")
    private MockEndpoint mockSubBSetBody;

    @EndpointInject("mock:subAOnException")
    private MockEndpoint mockSubAOnException;

    @EndpointInject("mock:subBOnException")
    private MockEndpoint mockSubBOnException;

    @EndpointInject("mock:mainOnException")
    private MockEndpoint mockMainOnException;

    private static boolean alreadySetup = false;

    @BeforeEach
    public void setupRoutes() throws Exception {
        MockEndpoint.resetMocks(camelContext);
        if (alreadySetup) {
            return;
        }

        adviceWith(camelContext, "MulticastMainRoute.timer", advice -> {
            advice.replaceFromWith("direct:hitme");
            advice.weaveById("MulticastMainRoute.beforeMulticast").before().to(mockMainBeforeMulticast);
            advice.weaveById("MulticastMainRoute.afterMulticast").after().to(mockMainAfterMulticast);
            advice.weaveAddLast().to("mock:mainDone");
            advice.weaveById("MulticastMainRoute.onException").after().to("mock:mainOnException");
        });

        adviceWith(camelContext, "MulticastSubARoute", advice -> {
            advice.weaveAddFirst().to("mock:subAInput");
            advice.weaveById("MulticastSubARoute.setBodyA").before().to(mockSubASetBody);
            advice.weaveAddLast().to("mock:subADone");
            advice.weaveById("MulticastSubARoute.onException").after().to("mock:subAOnException");
        });

        adviceWith(camelContext, "MulticastSubBRoute", advice -> {
            advice.weaveAddFirst().to("mock:subBInput");
            advice.weaveById("MulticastSubBRoute.setBodyB").before().to(mockSubBSetBody);
            advice.weaveAddLast().to("mock:subBDone");
            advice.weaveById("MulticastSubBRoute.onException").after().to("mock:subBOnException");
        });

        camelContext.start();
        alreadySetup = true;
    }

    private void isSatisfied() throws InterruptedException {
        mockSubAInput.assertIsSatisfied();
        mockSubBInput.assertIsSatisfied();
        mockSubADone.assertIsSatisfied();
        mockSubBDone.assertIsSatisfied();
        mockMainDone.assertIsSatisfied();
        mockSubAOnException.assertIsSatisfied();
        mockSubBOnException.assertIsSatisfied();
        mockMainOnException.assertIsSatisfied();
    }

    @Test
    public void testMulticastingSuccess() throws Exception {
        mockSubAInput.expectedBodiesReceived("original");
        mockSubBInput.expectedBodiesReceived("original");
        mockSubADone.expectedBodiesReceived("body A");
        mockSubAOnException.expectedMessageCount(0);
        mockSubBDone.expectedBodiesReceived("body B");
        mockSubBOnException.expectedMessageCount(0);
        mockMainDone.expectedBodiesReceived("original");
        mockMainOnException.expectedMessageCount(0);

        mockSubAInput.expectedHeaderReceived("testHeader", "header main");
        mockSubBInput.expectedHeaderReceived("testHeader", "header main");
        mockSubADone.expectedHeaderReceived("testHeader", "header A");
        mockSubBDone.expectedHeaderReceived("testHeader", "header B");
        mockMainDone.expectedHeaderReceived("testHeader", "header main");

        producerTemplate.sendBody("direct:hitme", "original");

        isSatisfied();
    }

    @Test
    public void testMulticastingSubAFails() throws Exception {

        mockSubAInput.expectedBodiesReceived("original");
        mockSubBInput.expectedBodiesReceived("original");
        mockSubADone.expectedMessageCount(0);
        mockSubAOnException.expectedMessageCount(1);
        mockSubBDone.expectedBodiesReceived("body B");
        mockSubBOnException.expectedMessageCount(0);
        mockMainDone.expectedBodiesReceived("original");
        mockMainOnException.expectedMessageCount(0);

        mockSubASetBody.whenAnyExchangeReceived(ex -> {
            throw new Exception("SubAFails");
        });

        mockSubAInput.expectedHeaderReceived("testHeader", "header main");
        mockSubBInput.expectedHeaderReceived("testHeader", "header main");
        mockSubBDone.expectedHeaderReceived("testHeader", "header B");
        mockMainDone.expectedHeaderReceived("testHeader", "header main");

        producerTemplate.sendBody("direct:hitme", "original");

        isSatisfied();
    }


    @Test
    public void testMulticastingSubBFails() throws Exception {

        mockSubAInput.expectedBodiesReceived("original");
        mockSubBInput.expectedBodiesReceived("original");
        mockSubADone.expectedBodiesReceived("body A");
        mockSubAOnException.expectedMessageCount(0);
        mockSubBDone.expectedMessageCount(0);
        mockSubBOnException.expectedMessageCount(1);
        mockMainDone.expectedBodiesReceived("original");
        mockMainOnException.expectedMessageCount(0);

        mockSubBSetBody.whenAnyExchangeReceived(ex -> {
            throw new Exception("SubBFails");
        });

        mockSubAInput.expectedHeaderReceived("testHeader", "header main");
        mockSubBInput.expectedHeaderReceived("testHeader", "header main");
        mockSubADone.expectedHeaderReceived("testHeader", "header A");
        mockMainDone.expectedHeaderReceived("testHeader", "header main");

        producerTemplate.sendBody("direct:hitme", "original");

        isSatisfied();

    }

    @Test
    public void testMulticastingMainFailsBeforeMulticast() throws Exception {

        mockSubAInput.expectedMessageCount(0);
        mockSubBInput.expectedMessageCount(0);
        mockSubADone.expectedMessageCount(0);
        mockSubAOnException.expectedMessageCount(0);
        mockSubBDone.expectedMessageCount(0);
        mockSubBOnException.expectedMessageCount(0);
        mockMainDone.expectedMessageCount(0);
        mockMainOnException.expectedMessageCount(1);

        mockMainBeforeMulticast.whenAnyExchangeReceived(ex -> {
            throw new Exception("MainFails before multicast");
        });

        Assertions.assertThrows(CamelExecutionException.class, () -> {
            producerTemplate.sendBody("direct:hitme", "original");
        });

        isSatisfied();
    }

    @Test
    public void testMulticastingMainFailsAfterMulticast() throws Exception {

        mockSubAInput.expectedBodiesReceived("original");
        mockSubBInput.expectedBodiesReceived("original");
        mockSubADone.expectedBodiesReceived("body A");
        mockSubAOnException.expectedMessageCount(0);
        mockSubBDone.expectedBodiesReceived("body B");
        mockSubBOnException.expectedMessageCount(0);
        mockMainDone.expectedMessageCount(0);
        mockMainOnException.expectedMessageCount(1);

        mockSubAInput.expectedHeaderReceived("testHeader", "header main");
        mockSubBInput.expectedHeaderReceived("testHeader", "header main");
        mockSubADone.expectedHeaderReceived("testHeader", "header A");
        mockSubBDone.expectedHeaderReceived("testHeader", "header B");

        mockMainAfterMulticast.whenAnyExchangeReceived(ex -> {
            throw new Exception("MainFails after multicast");
        });

        Assertions.assertThrows(CamelExecutionException.class, () -> {
            producerTemplate.sendBody("direct:hitme", "original");
        });

        isSatisfied();
    }

}
