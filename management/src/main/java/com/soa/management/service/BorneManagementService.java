package com.soa.management.service;

import com.soa.management.docker.DockerContainerManager;
import com.soa.management.registry.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BorneManagementService {
    private final BorneInstanceRepository repository;
    private final DockerContainerManager docker;
    private final String image;
    public BorneManagementService(BorneInstanceRepository repository, DockerContainerManager docker,
                                  @Value("${borne.image}") String image) {
        this.repository = repository; this.docker = docker; this.image = image;
    }
    @Transactional
    public List<BorneInstance> create(int count) {
        if (count < 1 || count > 100) throw new IllegalArgumentException("count must be between 1 and 100");
        reconcileMissingRegistrations();
        return java.util.stream.IntStream.range(0, count).mapToObj(i -> createOne(nextId())).toList();
    }
    @Transactional
    public BorneInstance createOne(String borneId) {
        if (repository.existsByBorneId(borneId)) throw new IllegalStateException("Borne already registered: " + borneId);
        String name = docker.containerName(borneId);
        BorneInstance instance = repository.save(new BorneInstance(borneId, name, image));
        try {
            var container = docker.createBorneContainer(borneId);
            instance.update(container.id(), ContainerStatus.CREATED, TechnicalHealthStatus.STARTING, 0, null);
            docker.startBorneContainer(container.id());
            return repository.save(instance);
        } catch (RuntimeException error) {
            instance.update(null, ContainerStatus.UNKNOWN, TechnicalHealthStatus.UNREACHABLE, 1, error.getMessage());
            repository.save(instance);
            throw error;
        }
    }
    public List<BorneInstance> list() { return repository.findAll(); }
    public BorneInstance find(String id) { return repository.findByBorneId(id).orElseThrow(() -> new IllegalArgumentException("Unknown Borne: " + id)); }
    public void start(String id) { docker.startBorneContainer(find(id).getContainerId()); }
    public void stop(String id) { docker.stopBorneContainer(find(id).getContainerId()); }
    public void restart(String id) { docker.restartBorneContainer(find(id).getContainerId()); }
    @Transactional public void remove(String id) {
        BorneInstance instance = find(id);
        if (instance.getContainerId() != null) {
            try {
                docker.removeBorneContainer(instance.getContainerId());
            } catch (RuntimeException ignored) {
                // A manually deleted container is already in the desired state.
            }
        }
        repository.delete(instance);
    }
    private void reconcileMissingRegistrations() {
        repository.findAll().stream()
                .filter(instance -> instance.getLastError() != null && instance.getLastError().contains("No such container"))
                .forEach(repository::delete);
    }
    private String nextId() {
        int i = 1;
        while (repository.existsByBorneId(String.format("B%03d", i))) i++;
        return String.format("B%03d", i);
    }
}
