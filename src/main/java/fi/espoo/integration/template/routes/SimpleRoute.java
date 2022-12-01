package fi.espoo.integration.template.routes;

import fi.espoo.integration.logging.core.VLogOption;
import fi.espoo.integration.template.EP;
import org.apache.camel.Expression;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.language.simple.SimpleLanguage;
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

    private static final String LN = "fi.espoo.integration.template.routes.SimpleRoute";
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleRoute.class);

    /**
     * Camel routes showing an example for splitting message and logging.
     *
     */
    @Override
    public void configure() throws Exception {

        from("timer:foo?period=5000")
            .routeId("SimpleRoute.timer")
            .autoStartup("{{SimpleRoute.timer.enabled}}")
            .log(LoggingLevel.DEBUG, LN, "SimpleRoute.timer triggered")
            .to("direct:setProperties")
            .to("vlog:New?info=Message ${exchangeProperty.vlogId} without body")
            .to("direct:process")
            .to("direct:split")
            .to("vlog:Done?info=Message ${exchangeProperty.vlogId} processing done");



        from("direct:setProperties")
            .routeId("SimpleRoute.setProperties")
            .setProperty(VLogOption.ID, simple("${bean:uuidGenerator.generateUuid()}"))
            .setProperty(VLogOption.SOURCE, simple("system.001"))
            .setProperty(VLogOption.DESTINATION, simple("system.002"))
            .setProperty(EP.RANDOM_UUID, simple("${bean:uuidGenerator.generateUuid()}"));



        from("direct:process")
            .routeId("SimpleRoute.process")
            .process(exchange -> {
                SimpleLanguage simple = (SimpleLanguage) exchange.getContext().resolveLanguage("simple");
                Expression expression = simple.createExpression("SimpleBuilder test: ${messageHistory} + \nHeaders: ${in.headers}");
                log.info("Simple.value=" + expression.evaluate(exchange, String.class));
            })
            .setBody(simple("hello,world")).id("SimpleRoute.process.setBody1")
            .to("vlog:Processing?info=Message ${exchangeProperty.vlogId} body set to ${body}");



        from("direct:split")
            .routeId("SimpleRoute.split")
            .split(body().tokenize(","))
                .setProperty("vlogCorrelationId", simple("${exchangeProperty.vlogId}"))
                .setProperty("vlogId", simple("${bean:uuidGenerator.generateUuid()}"))
                .to("vlog:New?info=New child message ${exchangeProperty.vlogId} " +
                        "with correlationId ${exchangeProperty.vlogCorrelationId} and body ${body}")
                .to("vlog:Done?info=Child message ${exchangeProperty.vlogId} done").id("SimpleRoute.split.lastlog")
                .end();
    }
}
