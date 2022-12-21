package org.test.code_scan_spike.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpMethods;
import org.apache.camel.language.simple.SimpleLanguage;
import org.apache.camel.processor.aggregate.UseOriginalAggregationStrategy;
import org.apache.camel.support.ExpressionAdapter;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

/**
 * Use javadoc comments to describe briefly what and why the class exists (if not self-evident).
 *
 */
@SuppressWarnings({"OperatorWrap"})
@Component
public class SimpleRoute extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleRoute.class);

    public CloseableHttpClient client;

    @PostConstruct
    public void initialize() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("tyranno", "saurus"));
        this.client = HttpClients.custom()
                .setDefaultCredentialsProvider(credentialsProvider)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setAuthenticationEnabled(true)
                        .setTargetPreferredAuthSchemes(Collections.singleton(AuthSchemes.BASIC))
                        .build()
                ).build();
    }

    @PreDestroy
    public void tearDown() {
        try {
            client.close();
        } catch (IOException e) {
            LOGGER.warn("Exception while closing http client.", e);
        }
    }

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
            .process(ex -> {
                String[] field = {"a", "b", "c", "s", "e"};
                String s = "";
                for (int i = 0; i < field.length; ++i) {
                    s = s + field[i];
                }
                ex.setProperty("string", s);
                
//                if (booleanMethod()) {
//                    ex.setProperty("boolean", true);
//                }
            })
            .setHeader("Authorization", new ExpressionAdapter() {
                @Override
                public Object evaluate(Exchange exchange) {
                    return authorization();
                }
            })
            .setHeader(Exchange.HTTP_METHOD, HttpMethods.GET)
            .to("http://localhost:8989/hello")
            .process(ex -> {
                HttpGet req = new HttpGet();
                req.setURI(new URI("http://localhost:8989/hello"));
                    try (CloseableHttpResponse res = this.client.execute(req)) {
                        ex.getIn().setBody(res.getEntity().getContent().readAllBytes());
                    }
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

    private Boolean booleanMethod() {
        return null;
    }

    private String authorization() {
        return "Basic " + Base64.getEncoder().encodeToString("tyranno:saurus".getBytes(StandardCharsets.UTF_8));
    }
}
