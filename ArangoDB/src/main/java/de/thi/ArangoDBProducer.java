package de.thi;

import com.arangodb.ArangoDB;
import com.arangodb.Protocol;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ArangoDBProducer {

    @ConfigProperty(name = "arangodb.host", defaultValue = "localhost")
    String host;

    @ConfigProperty(name = "arangodb.port", defaultValue = "8529")
    int port;


    @Produces
    @ApplicationScoped
    public ArangoDB arangoDB() {
        return new ArangoDB.Builder()
                .host(host, port)
                .protocol(Protocol.HTTP_JSON)
                .build();
    }
}
