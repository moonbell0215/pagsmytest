# wallet-transaction-processor

**這是**
* Spring cloud stream專案
* 實現商業邏輯的地方
* 負責處理寫交易請求
* 整個系統中唯一產生交易記錄的專案  
* 保證錢包交易記錄的準備性

**這不是**
* 不容許wallet-transaction-processor讀取Kafka以外的系統

**使用的技術**
* 使用Event Sourcing , 事件溯源設計模式
* Spring cloud stream
* Kafka Streams 
* Kafka

**流程**
1. WalletTransactionFunction.createTransactionCommandHandler() 從Kafka Topic Wallet.CreateTransactionCommand 拿到請求
1. processPlaceTradeCommand() 實現商業邏輯,先validate 要不要接受這個請求。
1. 假如接受
    1. 產生交易記錄, 寫到Kafka Topic Wallet.TransactionCreatedEvent
    1. 產生交易請求接受記錄,寫到Kafka Topic Wallet.CreateTransactionCommandProcessedEvent。這是方便webservice-dispatcher。
    1. 計算好各種各樣的餘額記錄,寫到Kafka Topic Wallet.BalanceUpdatedEvent。這是方便wallet-materialized-view-processor。
1. 假如不接受
    1. 產生流水請求不接受記錄,寫到Kafka Topic Wallet.CreateTransactionCommandProcessedEvent。這是方便webservice-dispatcher。


**Kafka Stream JSON DEMO**
1. 啟動Start Kafka
1. 啟動TransactionProcessorApplication
1. 開個Terminal 跑 kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic wallet.transactionCreatedEvent --property print.key=true --property key.separator="|"
1. 開個Terminal 跑 kafka-console-producer.bat --bootstrap-server localhost:9092 --topic wallet.createTransactionCommand --property parse.key=true --property key.separator="|"
1. 在 kafka-console-producer.bat Terminal 輸入 
* "tran-1"|{"transactionId":"tran-1","transactionAmount":10.1,"walletId":"wallet-1","transactionDateTime":"","transactionType":"DEPOSIT","description":"test"}
* "tran-2"|{"transactionId":"tran-2","transactionAmount":10.1,"walletId":"wallet-1","transactionDateTime":"","transactionType":"DEPOSIT","description":"test"}
* "tran-3"|{"transactionId":"tran-3","transactionAmount":10.1,"walletId":"wallet-2","transactionDateTime":"","transactionType":"DEPOSIT","description":"test"}
1. 假如wallet-materialized-view-processor 也在跑,在Cosmo DB會看到三條記錄。



**技術參考資料**
* https://www.imooc.com/article/40858
* https://cqrs.nu/Faq/commands-and-events
* https://kafka.apache.org/26/documentation/streams/developer-guide/
* https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/
* https://github.com/confluentinc/kafka-streams-examples/tree/6.0.1-post/


