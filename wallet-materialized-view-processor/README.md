# wallet-materialized-view-processor

**這是**
* Spring cloud stream專案
* 實現資料轉化邏輯的地方
* 負責把交易記錄,餘額記錄寫到Kafka 以外的Database,以方便查詢。暫只支援Azure CosmosDB 

**這不是**
* 刪改交易記錄的專案

**使用的技術**
* 使用Event Sourcing , 事件溯源設計模式中的materialized-view概念
* Spring cloud stream
* Kafka Streams 
* Kafka
* CosmosDB


**技術參考資料**
* https://dev.to/azure/going-full-reactive-with-spring-webflux-and-the-new-cosmosdb-api-v3-1n2a
* https://www.imooc.com/article/40858
* https://cqrs.nu/Faq/commands-and-events
* https://kafka.apache.org/26/documentation/streams/developer-guide/
* https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/
* https://github.com/confluentinc/kafka-streams-examples/tree/6.0.1-post/


