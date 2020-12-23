# webservice-inquiry-processor

**這是**
* Web service專案
* 負責把**查詢請求**轉化為CosmosDB query,轉發到CosmosDB。最後把查詢結果反回


**這不是**
* 實現商業邏輯的地方
* 檢証用戶的地方.檢証由第三方的API Gateway 完成，比如Azure API Gateway management. 當然,應解讀API Gateway的Token去得到請求者資訊
* 這專案不讀寫Kafka

**使用的技術**
* Spring Web Flex
* Azure CosmosDB SDK 4.0
* CosmosDB

**技術參考資料**
* https://docs.microsoft.com/zh-tw/azure/cosmos-db/sql-api-java-application
* https://github.com/Azure-Samples/azure-cosmos-java-getting-started
* https://dev.to/azure/going-full-reactive-with-spring-webflux-and-the-new-cosmosdb-api-v3-1n2a
* https://docs.microsoft.com/zh-tw/azure/cosmos-db/introduction
