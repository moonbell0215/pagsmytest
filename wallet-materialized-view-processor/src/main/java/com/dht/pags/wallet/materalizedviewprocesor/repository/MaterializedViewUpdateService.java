package com.dht.pags.wallet.materalizedviewprocesor.repository;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Collections;

@Service
public class MaterializedViewUpdateService {

    @Value("${application.cosmosdb.database}")
    private String databaseName;

    @Value("${application.cosmosdb.transaction}")
    private String containerTransaction;

    @Value("${application.cosmosdb.orderDetail}")
    private String containerOrderDetail;

    @Value("${application.cosmosdb.balance}")
    private String containerBalance;

    private final Logger LOGGER = LoggerFactory.getLogger(MaterializedViewUpdateService.class);

    @Value("${application.cosmosdb.accountHost}")
    private String accountHost;

    @Value("${application.cosmosdb.accountKey}")
    private String accountKey;

    private CosmosAsyncClient cosmosClient;
    private CosmosAsyncDatabase cosmosDatabase;
    private CosmosAsyncContainer cosmosContainer;

    @PostConstruct
    public void init() {
        this.getContainerCreateResourcesIfNotExist();
    }

    private CosmosAsyncContainer getContainerCreateResourcesIfNotExist() {
        LOGGER.info("Configuring CosmosDB connection");
        cosmosClient = new CosmosClientBuilder()
                .endpoint(accountHost)
                .key(accountKey)
                //  Setting the preferred location to Cosmos DB Account region
                //  West US is just an example. User should set preferred location to the Cosmos DB region closest to the application
                .preferredRegions(Collections.singletonList("Japan East"))
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                //  Setting content response on write enabled, which enables the SDK to return response on write operations.
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();

        if (cosmosDatabase == null) {
            cosmosClient.createDatabaseIfNotExists(databaseName);
            cosmosDatabase = cosmosClient.getDatabase(databaseName);
        }

        if (cosmosContainer == null) {
            CosmosContainerProperties properties = new CosmosContainerProperties(containerTransaction, "/walletId");
            cosmosDatabase.createContainerIfNotExists(properties);
            //cosmosContainer = cosmosDatabase.getContainer(containerId);
        }

        return cosmosContainer;
    }

    public <T> Mono<CosmosItemResponse<T>> createItem(T item) {
        cosmosContainer = cosmosDatabase.getContainer(containerTransaction);
        return cosmosContainer.createItem(item);
    }

    public <T> Mono<CosmosItemResponse<T>> createOrderDetail(T item) {
        cosmosContainer = cosmosDatabase.getContainer(containerOrderDetail);
        return cosmosContainer.createItem(item);
    }

    public <T> Mono<CosmosItemResponse<T>> updateBalance(T item) {
        cosmosContainer = cosmosDatabase.getContainer(containerBalance);
        return cosmosContainer.upsertItem(item);
    }
}
