package fi.espoo.integration.template.routes.multicast;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MulticastSubBRoute extends RouteBuilder {

    private static final String LN = "fi.espoo.integration.template.routes.multicast.MulticastSubBRoute";
    private static final Logger LOGGER = LoggerFactory.getLogger(MulticastSubBRoute.class);

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .handled(true)
                .log("exception on MulticastSubBRoute")
                .log("${exception.message}").id("MulticastSubBRoute.onException");

        from("direct:multicastSubB")
                .routeId("MulticastSubBRoute")
                .log(LoggingLevel.DEBUG, LN, "MulticastSubBRoute triggered")
                .to("vlog:Processing?info=Message ${exchangeProperty.vlogId} with body: ${body}")
                .to("vlog:Processing?info=Header 'testHeader' with value: ${headers.testHeader}")
                .setHeader("testHeader", simple("header B"))
                .to("vlog:Processing?info=Header 'testHeader' set to: ${headers.testHeader}")
                .setBody(simple("body B")).id("MulticastSubBRoute.setBodyB")
                .to("vlog:Processing?info=Body set to: ${body}");
    }
}
