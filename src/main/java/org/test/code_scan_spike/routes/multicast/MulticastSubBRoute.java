package org.test.code_scan_spike.routes.multicast;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MulticastSubBRoute extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MulticastSubBRoute.class);

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .handled(true)
                .log("exception on MulticastSubBRoute")
                .log("${exception.message}").id("MulticastSubBRoute.onException");

        from("direct:multicastSubB")
                .routeId("MulticastSubBRoute")
                .log(LoggingLevel.DEBUG, LOGGER, "MulticastSubBRoute triggered")
                .log(LoggingLevel.INFO, LOGGER, "Processing: Message ${exchangeProperty.logId} with body: ${body}")
                .log(LoggingLevel.INFO, LOGGER, "Processing: Header 'testHeader' with value: ${headers.testHeader}")
                .setHeader("testHeader", simple("header B"))
                .log(LoggingLevel.INFO, LOGGER, "Processing: Header 'testHeader' set to: ${headers.testHeader}")
                .setBody(simple("body B")).id("MulticastSubBRoute.setBodyB")
                .log(LoggingLevel.INFO, LOGGER, "Processing: Body set to: ${body}");
    }
}
