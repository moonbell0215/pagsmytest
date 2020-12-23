package com.dht.pags.wallet.webservice.inquiryprocessor;

import com.azure.data.cosmos.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;

@Service
@RestController
@RequestMapping("/wallets")
public class InquiryProcessorController {

    @Value("${application.cosmosdb.database}")
    private String databaseName;

    @Value("${application.cosmosdb.container}")
    private String containerName;

    private final Logger log = LoggerFactory.getLogger(InquiryProcessorController.class);

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
                .publishOn(Schedulers.parallel())
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
                .publishOn(Schedulers.parallel())
                .block();

        container = database.getContainer(containerName);

    }

    @GetMapping("/")
    public Flux<FeedResponse<CosmosItemProperties>> getAllItems() {
        return container.queryItems("Select * from c");
    }
}
