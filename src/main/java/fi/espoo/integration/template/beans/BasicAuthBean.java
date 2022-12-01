package fi.espoo.integration.template.beans;

import org.apache.camel.Exchange;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BasicAuthBean {

    private final String username;
    private final String password;

    /**
     * Set Basic Authorization header to exchange.
     *
     * @param username
     * @param password
     */
    public BasicAuthBean(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     *  Set Basic Authorization header to exchange.
     *
     * @param exchange
     * @throws Exception
     */
    public void setAuthHeader(Exchange exchange) throws Exception {
        String toEncode = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(toEncode.getBytes(StandardCharsets.UTF_8));
        exchange.getIn().setHeader("Authorization", "Basic " + encoded);
    }
}
