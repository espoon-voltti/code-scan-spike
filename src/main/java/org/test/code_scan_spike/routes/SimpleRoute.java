package org.test.code_scan_spike.routes;

import org.apache.camel.Expression;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.language.simple.SimpleLanguage;
import org.apache.camel.processor.aggregate.UseOriginalAggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Use javadoc comments to describe briefly what and why the class exists (if not self-evident).
 *
 */
@SuppressWarnings({"OperatorWrap"})
@Component
public class SimpleRoute extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleRoute.class);

    /**
     * Camel routes showing an example for splitting message.
     *
     */
    @Override
    public void configure() throws Exception {

        from("timer:foo?period=5000")
            .routeId("SimpleRoute.timer")
            .autoStartup("{{SimpleRoute.timer.enabled}}")
            .log(LoggingLevel.DEBUG, LOGGER, "SimpleRoute.timer triggered")
            .to("direct:setProperties")
            .log(LoggingLevel.INFO, LOGGER, "New: Message ${exchangeProperty.logId} without body")
            .to("direct:process")
            .to("direct:split")
            .log(LoggingLevel.INFO, LOGGER, "Done: Message ${exchangeProperty.logId} processing done");


        from("direct:setProperties")
                .routeId("SimpleRoute.setProperties")
                .setProperty("logId", simple("${bean:uuidGenerator.generateUuid()}"))
                .setProperty("source", simple("source"))
                .setProperty("destination", simple("destination"))
                .setProperty("randomUuid", simple("${bean:uuidGenerator.generateUuid()}"));

        from("direct:process")
            .routeId("SimpleRoute.process")
            .process(exchange -> {
                SimpleLanguage simple = (SimpleLanguage) exchange.getContext().resolveLanguage("simple");
                Expression expression = simple.createExpression("SimpleBuilder test: ${messageHistory} + \nHeaders: ${in.headers}");
                LOGGER.info("Simple.value=" + expression.evaluate(exchange, String.class));
            })
            .setBody(simple("hello,world")).id("SimpleRoute.process.setBody1")
            .log(LoggingLevel.INFO, LOGGER, "Processing: Message ${exchangeProperty.logId} body set to ${body}");



        from("direct:split")
            .routeId("SimpleRoute.split")
            .split(body().tokenize(",")).aggregationStrategy(new UseOriginalAggregationStrategy())
                .setProperty("correlationId", exchangeProperty("logId"))
                .setProperty("logId", simple("${bean:uuidGenerator.generateUuid()}"))
                .log(LoggingLevel.INFO, LOGGER, "New: New child message ${exchangeProperty.logId} " +
                        "with correlationId ${exchangeProperty.correlationId} and body ${body}")
                .log(LoggingLevel.INFO, LOGGER, "Done: Child message ${exchangeProperty.logId} done").id("SimpleRoute.split.lastlog")
            .end();
    }
}
