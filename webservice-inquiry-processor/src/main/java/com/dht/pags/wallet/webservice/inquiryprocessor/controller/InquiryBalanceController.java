package com.dht.pags.wallet.webservice.inquiryprocessor.controller;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.dht.pags.wallet.webservice.inquiryprocessor.domain.Balance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.Collections;

@Service
@RestController
@RequestMapping("/wallets")
public class InquiryBalanceController {
    private final Logger logger = LoggerFactory.getLogger(InquiryBalanceController.class);

    @Value("${application.cosmosdb.accountHost}")
    private String accountHost;

    @Value("${application.cosmosdb.accountKey}")
    private String accountKey;

    @Value("${application.cosmosdb.database}")
    private String databaseName;

    @Value("${application.cosmosdb.container.balance}")
    private String containerId;

    private CosmosAsyncClient cosmosClient;
    private CosmosAsyncDatabase cosmosDatabase;
    private CosmosAsyncContainer cosmosContainer;

    @GetMapping("/balances")
    public Flux<Balance> getAllItems() {

        CosmosPagedFlux<Balance> pagedFluxResponse =
                cosmosContainer.queryItems("Select * from c", new CosmosQueryRequestOptions(), Balance.class);

        return pagedFluxResponse;
    }

    @GetMapping("/balances/{walletId}")
    public Flux<Balance> getItemById(@PathVariable String walletId) {

        CosmosPagedFlux<Balance> pagedFluxResponse =
                cosmosContainer.queryItems("Select * from c where c.walletId IN ('"+walletId+"') ORDER BY c.updateTime DESC OFFSET 0 LIMIT 1", new CosmosQueryRequestOptions(), Balance.class);

        return pagedFluxResponse;
    }


    @PostConstruct
    public void init() {
        this.getContainerCreateResourcesIfNotExist();
    }

    private CosmosAsyncContainer getContainerCreateResourcesIfNotExist() {
        logger.info("Configuring CosmosDB connection");
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
            CosmosContainerProperties properties = new CosmosContainerProperties(containerId, "/walletId");
            cosmosDatabase.createContainerIfNotExists(properties);
            cosmosContainer = cosmosDatabase.getContainer(containerId);
        }

        return cosmosContainer;
    }
}
