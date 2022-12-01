package org.test.code_scan_spike.routes.multicast;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MulticastSubARoute extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MulticastSubARoute.class);

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .handled(true)
                .log("exception on MulticastSubARoute")
                .log("${exception.message}").id("MulticastSubARoute.onException");

        from("direct:multicastSubA")
                .routeId("MulticastSubARoute")
                .log(LoggingLevel.DEBUG, LOGGER, "MulticastSubARoute triggered")
                .log(LoggingLevel.INFO, LOGGER, "Processing: Message ${exchangeProperty.logId} with body: ${body}")
                .log(LoggingLevel.INFO, LOGGER, "Processing: Header 'testHeader' with value: ${headers.testHeader}")
                .setHeader("testHeader", simple("header A"))
                .log(LoggingLevel.INFO, LOGGER, "Processing: Header 'testHeader' set to: ${headers.testHeader}")
                .setBody(simple("body A")).id("MulticastSubARoute.setBodyA")
                .log(LoggingLevel.INFO, LOGGER, "Processing: Body set to: ${body}");
    }
}
