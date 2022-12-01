package org.test.code_scan_spike.routes.multicast;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.UseOriginalAggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MulticastMainRoute extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MulticastMainRoute.class);

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .log("exception on MulticastMainRoute")
                .log("${exception.message}").id("MulticastMainRoute.onException");

        from("timer:bar?repeatCount=1")
                .routeId("MulticastMainRoute.timer")
                .autoStartup("{{MulticastMainRoute.timer.enabled}}")
                .log(LoggingLevel.DEBUG, LOGGER, "MulticastMainRoute.timer triggered")
                .setProperty("logId", method("uuidGenerator", "generateUuid"))
                .log(LoggingLevel.INFO, LOGGER, "New: Message ${exchangeProperty.logId} with body: ${body}")
                .setHeader("testHeader", simple("header main")).id("MulticastMainRoute.beforeMulticast")
                .multicast().aggregationStrategy(new UseOriginalAggregationStrategy())
                    .to("direct:multicastSubA")
                    .to("direct:multicastSubB")
                .end().id("MulticastMainRoute.afterMulticast")
                .log(LoggingLevel.INFO, LOGGER, "Processing: Message ${exchangeProperty.logId} with body: ${body}")
                .log(LoggingLevel.INFO, LOGGER, "Processing: Header 'testHeader' with value: ${headers.testHeader}")
                .log(LoggingLevel.INFO, LOGGER, "Done: Message ${exchangeProperty.logId} processing done");

    }
}
