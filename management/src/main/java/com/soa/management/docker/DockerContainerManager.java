package com.soa.management.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.net.URI;

@Component
public class DockerContainerManager implements AutoCloseable {
    public record BorneContainer(String id, String name, String status, String health, Map<String, ContainerNetwork> networks) {}
    private final DockerClient client;
    private final String network;
    private final String image;
    private final String databaseUrl;
    private final String databaseUsername;
    private final String databasePassword;
    private final String mqttBrokerUrl;
    private final String mqttBornePassword;

    public DockerContainerManager(@Value("${borne.network}") String network, @Value("${borne.image}") String image,
                                  @Value("${spring.datasource.url}") String databaseUrl,
                                  @Value("${spring.datasource.username}") String databaseUsername,
                                  @Value("${spring.datasource.password}") String databasePassword,
                                  @Value("${mqtt.broker-url:tcp://mosquitto:1883}") String mqttBrokerUrl,
                                  @Value("${mqtt.borne-password:change-me}") String mqttBornePassword) {
        this.network = network; this.image = image; this.databaseUrl = databaseUrl;
        this.databaseUsername = databaseUsername; this.databasePassword = databasePassword;
        this.mqttBrokerUrl = mqttBrokerUrl; this.mqttBornePassword = mqttBornePassword;
        var config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(System.getenv().getOrDefault("DOCKER_HOST", "unix:///var/run/docker.sock"))
                .build();
        var http = new ZerodepDockerHttpClient.Builder()
                .dockerHost(URI.create(System.getenv().getOrDefault("DOCKER_HOST", "unix:///var/run/docker.sock")))
                .sslConfig(config.getSSLConfig()).build();
        this.client = DockerClientImpl.getInstance(config, http);
    }

    public BorneContainer createBorneContainer(String borneId) {
        String name = containerName(borneId);
        if (!client.listContainersCmd().withShowAll(true).withNameFilter(List.of(name)).exec().isEmpty()) {
            throw new IllegalStateException("Container already exists: " + name);
        }
        ExposedPort port = ExposedPort.tcp(8080);
        HostConfig host = HostConfig.newHostConfig().withNetworkMode(network);
        CreateContainerResponse created = client.createContainerCmd(image).withName(name)
                .withEnv("BORNE_ID=" + borneId, "SERVER_PORT=8080",
                        "FLYWAY_ENABLED=false", "BORNE_INITIALIZER_ENABLED=true",
                        "DB_URL=" + databaseUrl, "DB_USERNAME=" + databaseUsername,
                        "DB_PASSWORD=" + databasePassword, "MQTT_BROKER_URL=" + mqttBrokerUrl,
                        "MQTT_USERNAME=" + borneId, "MQTT_PASSWORD=" + mqttBornePassword,
                        "MQTT_CLIENT_ID=borne-" + borneId)
                .withExposedPorts(port).withHostConfig(host).withLabels(Map.of("soa.borne", "true")).exec();
        return inspectBorneContainer(created.getId());
    }

    public void startBorneContainer(String id) { client.startContainerCmd(resolve(id)).exec(); }
    public void stopBorneContainer(String id) { client.stopContainerCmd(resolve(id)).withTimeout(10).exec(); }
    public void restartBorneContainer(String id) { client.restartContainerCmd(resolve(id)).withTimeout(10).exec(); }
    public void removeBorneContainer(String id) { client.removeContainerCmd(resolve(id)).withForce(true).exec(); }
    public BorneContainer getBorneContainerStatus(String id) { return inspectBorneContainer(resolve(id)); }
    public List<BorneContainer> listBorneContainers() {
        return client.listContainersCmd().withShowAll(true).withLabelFilter(Map.of("soa.borne", "true")).exec()
                .stream().map(this::map).toList();
    }
    public BorneContainer inspectBorneContainer(String id) { return map(client.inspectContainerCmd(resolve(id)).exec()); }
    public String containerName(String borneId) { return "borne-" + borneId.toLowerCase().replaceAll("[^a-z0-9_.-]", "-"); }

    private String resolve(String idOrName) {
        if (idOrName == null || idOrName.isBlank()) throw new IllegalArgumentException("Container identifier is required");
        return idOrName;
    }
    private BorneContainer map(Container c) {
        String status = c.getStatus() == null ? "UNKNOWN" : c.getStatus();
        return new BorneContainer(c.getId(), c.getNames() == null || c.getNames().length == 0 ? "" : c.getNames()[0],
                status, c.getState(), Map.of());
    }
    private BorneContainer map(com.github.dockerjava.api.command.InspectContainerResponse c) {
        var state = c.getState();
        String status = state == null ? "UNKNOWN" : state.getStatus();
        String health = state == null || state.getHealth() == null ? null : state.getHealth().getStatus();
        return new BorneContainer(c.getId(), c.getName(), status, health,
                c.getNetworkSettings() == null || c.getNetworkSettings().getNetworks() == null
                        ? Map.of() : c.getNetworkSettings().getNetworks());
    }
    @Override public void close() {
        try {
            client.close();
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Unable to close Docker client", exception);
        }
    }
}
