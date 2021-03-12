package com.dht.pags.wallet.materalizedview.recordchecker.repository;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.dht.pags.wallet.domain.Balance;
import com.dht.pags.wallet.domain.BalanceUpdatedEvent;
import com.dht.pags.wallet.domain.TransactionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MaterializedViewCompareService {

    @Value("${application.cosmosdb.database}")
    private String databaseName;

    @Value("${application.cosmosdb.container.transaction}")
    private String containerTransaction;

    @Value("${application.cosmosdb.container.orderDetail}")
    private String containerOrderDetail;

    @Value("${application.cosmosdb.container.balance}")
    private String containerBalance;

    @Value("${application.cosmosdb.container.test}")
    private String containerTest;

    private final Logger LOGGER = LoggerFactory.getLogger(MaterializedViewCompareService.class);

    @Value("${application.cosmosdb.accountHost}")
    private String accountHost;

    @Value("${application.cosmosdb.accountKey}")
    private String accountKey;

    private CosmosClient cosmosClient;
    private CosmosDatabase cosmosDatabase;
    private CosmosContainer cosmosContainer;

    @PostConstruct
    public void init() {
        this.getContainerCreateResourcesIfNotExist();
    }

    private CosmosContainer getContainerCreateResourcesIfNotExist() {
        LOGGER.info("Configuring CosmosDB connection");
        cosmosClient = new CosmosClientBuilder()
                .endpoint(accountHost)
                .key(accountKey)
                //  Setting the preferred location to Cosmos DB Account region
                //  West US is just an example. User should set preferred location to the Cosmos DB region closest to the application
                //.preferredRegions(Collections.singletonList("Japan East"))
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                //  Setting content response on write enabled, which enables the SDK to return response on write operations.
                .contentResponseOnWriteEnabled(true)
                .buildClient();

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

    public <T> CosmosItemResponse createItem(T item) {
        cosmosContainer = cosmosDatabase.getContainer(containerTransaction);
        //cosmosContainer = cosmosDatabase.getContainer(containerTest);
        return cosmosContainer.createItem(item);
    }

    public <T> CosmosItemResponse createOrderDetail(T item) {
        cosmosContainer = cosmosDatabase.getContainer(containerOrderDetail);
        //cosmosContainer = cosmosDatabase.getContainer(containerTest);
        return cosmosContainer.createItem(item);
    }

    public <T> CosmosItemResponse updateBalance(T item) {
        cosmosContainer = cosmosDatabase.getContainer(containerBalance);
        //cosmosContainer = cosmosDatabase.getContainer(containerTest);
        return cosmosContainer.upsertItem(item);
    }

    public <T> boolean readItem(T item) {
        boolean isExite = true;

        try {
            if (item instanceof TransactionCreatedEvent) {
                cosmosContainer = cosmosDatabase.getContainer(containerTransaction);
                //cosmosContainer = cosmosDatabase.getContainer(containerTest);
                cosmosContainer.readItem(((TransactionCreatedEvent) item).getId(),new PartitionKey(((TransactionCreatedEvent) item).getWalletId()),TransactionCreatedEvent.class).getItem();
            } else if (item instanceof BalanceUpdatedEvent) {
                cosmosContainer = cosmosDatabase.getContainer(containerOrderDetail);
                //cosmosContainer = cosmosDatabase.getContainer(containerTest);
                cosmosContainer.readItem(((BalanceUpdatedEvent) item).getId(),new PartitionKey(((BalanceUpdatedEvent) item).getWalletId()),BalanceUpdatedEvent.class).getItem();
            }

        } catch (CosmosException ex) {
            if (ex.getStatusCode() == 404) {
                isExite = false;
            }
        }
        return isExite;
    }

    public long getLastupdateTime(BalanceUpdatedEvent balanceUpdatedEvent) {
        long lastUpdateTime = 0;
        Balance balance;
        cosmosContainer = cosmosDatabase.getContainer(containerBalance);
        try {
            balance = cosmosContainer.readItem(balanceUpdatedEvent.getWalletId(),new PartitionKey(balanceUpdatedEvent.getWalletId()), Balance.class).getItem();
            lastUpdateTime = balance.getUpdateTime();
        } catch (CosmosException ex) {
            if (ex.getStatusCode() == 404) {
                System.out.println("!!!!!!!!!!!!!!!!!!!!! " + balanceUpdatedEvent.getWalletId() + " Balance not create yet ");
            }
        }
        return lastUpdateTime;
    }
}
