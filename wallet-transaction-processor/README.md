# wallet-transaction-processor

**這是**
* Spring cloud stream專案
* 實現商業邏輯的地方
* 負責處理寫流水請求
* 整個系統中唯一產生流水記錄的專案  
* 保證錢包流水記錄的準備性

**這不是**
* 不容許wallet-transaction-processor讀取Kafka以外的系統

**使用的技術**
* 使用Event Sourcing , 事件溯源設計模式
* Spring cloud stream
* Kafka Streams 
* Kafka

**技術參考資料**
* https://www.imooc.com/article/40858
* https://cqrs.nu/Faq/commands-and-events
* https://kafka.apache.org/26/documentation/streams/developer-guide/
* https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/
* https://github.com/confluentinc/kafka-streams-examples/tree/6.0.1-post/


