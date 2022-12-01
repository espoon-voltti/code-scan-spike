package fi.espoo.integration.template.routes.multicast;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MulticastSubARoute extends RouteBuilder {

    private static final String LN = "fi.espoo.integration.template.routes.multicast.MulticastSubARoute";
    private static final Logger LOGGER = LoggerFactory.getLogger(MulticastSubARoute.class);

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .handled(true)
                .log("exception on MulticastSubARoute")
                .log("${exception.message}").id("MulticastSubARoute.onException");

        from("direct:multicastSubA")
                .routeId("MulticastSubARoute")
                .log(LoggingLevel.DEBUG, LN, "MulticastSubARoute triggered")
                .to("vlog:Processing?info=Message ${exchangeProperty.vlogId} with body: ${body}")
                .to("vlog:Processing?info=Header 'testHeader' with value: ${headers.testHeader}")
                .setHeader("testHeader", simple("header A"))
                .to("vlog:Processing?info=Header 'testHeader' set to: ${headers.testHeader}")
                .setBody(simple("body A")).id("MulticastSubARoute.setBodyA")
                .to("vlog:Processing?info=Body set to: ${body}");
    }
}
