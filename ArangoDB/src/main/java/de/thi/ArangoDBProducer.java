package de.thi;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
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

    @ConfigProperty(name = "arangodb.database", defaultValue = "_system")
    String databaseName;

    @Produces
    @ApplicationScoped
    public ArangoDB arangoDB() {
        return new ArangoDB.Builder()
                .host(host, port)
                .protocol(Protocol.HTTP_JSON)
                .build();
    }

    @Produces
    @ApplicationScoped
    public ArangoDatabase arangoDatabase(ArangoDB arangoDB) {
        return arangoDB.db(databaseName);
    }
}
