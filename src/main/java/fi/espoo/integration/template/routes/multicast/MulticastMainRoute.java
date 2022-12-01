package fi.espoo.integration.template.routes.multicast;

import fi.espoo.integration.logging.core.VLogOption;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.UseOriginalAggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MulticastMainRoute extends RouteBuilder {

    private static final String LN = "fi.espoo.integration.template.routes.multicast.MulticastMainRoute";
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
                .setProperty(VLogOption.ID, method("uuidGenerator", "generateUuid"))
                .to("vlog:New?info=Message ${exchangeProperty.vlogId} with body: ${body}")
                .setHeader("testHeader", simple("header main")).id("MulticastMainRoute.beforeMulticast")
                .multicast().aggregationStrategy(new UseOriginalAggregationStrategy())
                    .to("direct:multicastSubA")
                    .to("direct:multicastSubB")
                .end().id("MulticastMainRoute.afterMulticast")
                .to("vlog:Processing?info=Message ${exchangeProperty.vlogId} with body: ${body}")
                .to("vlog:Processing?info=Header 'testHeader' with value: ${headers.testHeader}")
                .to("vlog:Done?info=Message ${exchangeProperty.vlogId} processing done");

    }
}
