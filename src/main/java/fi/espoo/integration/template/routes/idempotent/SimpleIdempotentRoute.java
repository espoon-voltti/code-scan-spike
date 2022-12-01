package fi.espoo.integration.template.routes.idempotent;

import fi.espoo.integration.logging.core.VLogOption;
import fi.espoo.integration.camelutils.idempotent.JdbcIdempotentRepositoryFactory;
import fi.espoo.integration.camelutils.idempotent.JdbcMessageIdRepository;
import fi.espoo.integration.template.routes.SimpleRoute;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings({"checkstyle:NoWhitespaceBefore"})
@Component
public class SimpleIdempotentRoute extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleRoute.class);

    @Autowired
    private JdbcIdempotentRepositoryFactory idempotentRepositoryFactory;

    @Override
    public void configure() throws Exception {
        JdbcMessageIdRepository someHeaderIdempotentRepository = idempotentRepositoryFactory
                .getJdbcMessageIdRepositoryInstance("SimpleIdempotentRoute.timer.header.someHeader");

        // @formatter:off
        from("timer:SimpleIdempotentRoute?repeatCount=2")
                .routeId("SimpleIdempotentRoute.timer")
                .autoStartup("{{SimpleIdempotentRoute.timer.enabled}}")
                .setProperty(VLogOption.ID, method("uuidGenerator", "generateUuid"))
                .log(LoggingLevel.DEBUG, LOGGER, "SimpleIdempotentRoute.timer triggered")
                .setHeader("someHeader", constant("1")).id("SimpleIdempotentRoute.timer.header")
                .idempotentConsumer(header("someHeader"), someHeaderIdempotentRepository)
                    .log(LoggingLevel.DEBUG, LOGGER, "SimpleIdempotentRoute.timer idempotent: ${header.someHeader}")
                    .id("SimpleIdempotentRoute.timer.idempotent")
                .end() // idempotent ends here
        ;
        // @formatter:on

        // @formatter:off
        from("direct:removeKey")
                .routeId("SimpleIdempotentRoute.removeKey")
                .autoStartup("{{SimpleIdempotentRoute.timer.enabled}}")
                .setProperty(VLogOption.ID, method("uuidGenerator", "generateUuid"))
                .setHeader("someHeader", constant("1"))
                .process(idempotentRepositoryFactory
                        .getIdempotentDeleteProcessor("SimpleIdempotentRoute.timer.header.someHeader")
                        .deleteWhen(constant(true))
                        .messageId(simple("${header.someHeader}"))
                        .processor());

        // @formatter:on

    }
}
