# webservice-dispatcher

**這是**
* Web service專案
* 負責把**要求寫交易請求**轉發到Kafka。它亦會監聽Kafka,把處理結果反回給Web service user。

**這不是**
* 實現商業邏輯的地方
* 檢証用戶的地方。檢証由第三方的API Gateway 完成，比如Azure API Gateway management。當然,應解讀API Gateway的Token去得到請求者資訊。

**使用的技術**
* Spring Web Flex
* Reactive Kafka

**技術參考資料**
* https://blog.csdn.net/weixin_43932886/article/details/89505965
