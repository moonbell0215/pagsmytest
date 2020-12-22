package com.dht.pags.wallet.materalizedviewprocesor.repository;

import com.azure.data.cosmos.*;
import com.dht.pags.wallet.materalizedviewprocesor.domain.Balance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Service
public class MaterializedViewUpdateService {

    @Value("${application.cosmosdb.accountHost}")
    private String databaseName;

    @Value("${application.cosmosdb.container")
    private String containerName;

    private final Logger log = LoggerFactory.getLogger(MaterializedViewUpdateService.class);

    @Value("${application.cosmosdb.accountHost}")
    private String accountHost;

    @Value("${application.cosmosdb.accountKey}")
    private String accountKey;

    private CosmosClient client;
    private CosmosDatabase database;
    private CosmosContainer container;

    @PostConstruct
    public void init() {
        log.info("Configuring CosmosDB connection");

        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.DIRECT);

        client = CosmosClient.builder()
                .endpoint(accountHost)
                .key(accountKey)
                .connectionPolicy(connectionPolicy)
                .build();

        // Create the database if it does not exist yet
        client.createDatabaseIfNotExists(databaseName)
                .doOnSuccess(cosmosDatabaseResponse -> log.info("Database: " + cosmosDatabaseResponse.database().id()))
                .doOnError(throwable -> log.error(throwable.getMessage()))
                .publishOn(Schedulers.elastic())
                .block();

        database = client.getDatabase(databaseName);

        // Create the container if it does not exist yet
        CosmosContainerProperties containerSettings = new CosmosContainerProperties(containerName, "/id");
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.automatic(false);
        containerSettings.indexingPolicy(indexingPolicy);
        database.createContainerIfNotExists(containerSettings, 400)
                .doOnSuccess(cosmosContainerResponse -> log.info("Container: " + cosmosContainerResponse.container().id()))
                .doOnError(throwable -> log.error(throwable.getMessage()))
                .publishOn(Schedulers.elastic())
                .block();

        container = database.getContainer(containerName);

    }

    public Mono<Balance> save(TransactionCreatedEvent balance) {
        balance.setId(UUID.randomUUID().toString());
        return container.createItem(balance)
                .map(i -> {
                    balance.setEmployeecode("test123");
                    balance.setUpdatetime("20201223120000");
                    balance.setBalance(100.0);
                    //balance.setName(i.properties().getString("name"));
                    return balance;
                });
    }

    public Mono<Balance> findById(String employeecode) {
        return container.getItem(employeecode, employeecode).read().map(i -> {
            Balance balance = new Balance();
            balance.setEmployeecode(employeecode);
            balance.setUpdatetime(i.properties().getString("updatetime"));
            balance.setBalance(i.properties().getDouble("balance"));
            //balance.setId(i.item().id());
            //balance.setName(i.properties().getString("name"));
            return balance;
        });
    }
}