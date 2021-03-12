package com.dht.pags.wallet.materalizedview.recordchecker.fn;

import com.dht.pags.wallet.domain.Balance;
import com.dht.pags.wallet.domain.BalanceUpdatedEvent;
import com.dht.pags.wallet.domain.TransactionCreatedEvent;
import com.dht.pags.wallet.materalizedview.recordchecker.repository.MaterializedViewCompareService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;


@Service
public class CheckTransactionRecordFunction {

    @Autowired
    private MaterializedViewCompareService materializedViewCompareService;

    private Logger LOGGER = LoggerFactory.getLogger(CheckTransactionRecordFunction.class);
    private String IN_TOPIC = "wallet.createTransactionCommand";
    private String OUT_TOPIC = "wallet.createTransactionCommandProcessedEvent";
    private String SUCCESS_TRANSACTION_TOPIC_NAME ="wallet.transactionCreatedEvent";
    private String BALANCE_UPDATED_TOPIC_NAME="wallet.balanceUpdatedEvent";
    private String STORE_NAME="TransactionCreatedEventStore";

    private KafkaConsumer kafkaConsumer = null;
    private Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();

    @PostConstruct
    public void init() {
        this.recordChecker();
    }

    public void recordChecker() {

            List<String> topics = new ArrayList<String>();
            topics.add(SUCCESS_TRANSACTION_TOPIC_NAME);
            topics.add(BALANCE_UPDATED_TOPIC_NAME);

            Properties properties = new Properties();
            UUID uuid = UUID.randomUUID();
            //连接broker集群地址，多个broker逗号隔开
            //本機
            //properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "psrc-e8vk0.southeastasia.azure.confluent.cloud:9092");
            //配置消费者所属的分组id
            properties.put(ConsumerConfig.GROUP_ID_CONFIG, "transaction.records.check.test001");
            //反序列化key
            properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
            //反序列化value
            properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.connect.json.JsonDeserializer");
            //消费者加入group时，消费offset设置策略，earliest重置offset为最早的偏移地址，latest重置ofsset为已经消费的最大偏移，none没有offset时抛异常
            properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            //每次允许拉取最大的消息数量
            properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);

            kafkaConsumer = new KafkaConsumer<String, JsonNode>(properties);
            kafkaConsumer.subscribe(topics);

            int counter = 0;
            try {
                while (true) {
                    //拉取消息，暂时不确认消息
                    //kafkaConsumer.poll(0);
                    //kafkaConsumer.seekToBeginning(kafkaConsumer.assignment());
                    ConsumerRecords<String, JsonNode> consumerRecord = kafkaConsumer.poll(Duration.ofMillis(1000 * 5));
                    System.out.println("------------------------ " + consumerRecord.count() + " message is waiting to check -------------------------");
                    Thread.sleep(30 * 1000);
                    System.out.println("------------------------ start checking ------------------------");
                    boolean isExist = true;

                    for (ConsumerRecord<String, JsonNode> record : consumerRecord) {
                        try {
                            counter += 1;
                            if (record.offset() == 578) {
                                Balance balance = null;
                                if (balance.getWalletId() == "") {

                                }
                            }
                            //System.out.println("-------------message receive:"+record.value());
                            //BalanceUpdatedEvent balanceUpdatedEvent = JSON.parseObject(record.value().toString(), BalanceUpdatedEvent.class);
                            //BalanceUpdatedEvent balanceUpdatedEvent = record.value();
                            JsonNode jsonNode = record.value();
                            if (jsonNode.has("orderId")) {
                                transactionRecordHandler(jsonNode);
                            } else {
                                balanceUpdateRecordHandler(jsonNode);
                            }

                            //System.out.println("topic = " + record.topic());
                            //System.out.println("offset = " + record.offset());
                            kafkaConsumer.commitSync();
                            kafkaConsumer.commitAsync((offsets, exception) -> {
                                if (exception != null)
                                    System.out.println(exception);
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                            //System.out.println("error : " + e);
                            TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
                            Long committedOffset = record.offset();
                            kafkaConsumer.seek(topicPartition, committedOffset);
                            System.out.println("last committed offset: " + committedOffset);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                kafkaConsumer.close();
            }
        }


        public CheckTransactionRecordFunction() {
        }

        private void transactionRecordHandler(JsonNode jsonNode) throws JsonProcessingException {
            boolean isExist = true;
            ObjectMapper mapper = new ObjectMapper();
            TransactionCreatedEvent transactionCreatedEvent = mapper.treeToValue(jsonNode,TransactionCreatedEvent.class);
            isExist = materializedViewCompareService.readItem(transactionCreatedEvent);
            if(!isExist) {
                materializedViewCompareService.createItem(new TransactionCreatedEvent(transactionCreatedEvent.getId(),
                        transactionCreatedEvent.getOrderId(),
                        transactionCreatedEvent.getTransactionAmount(),
                        transactionCreatedEvent.getWalletId(),
                        transactionCreatedEvent.getTransactionDateTime(),
                        transactionCreatedEvent.getTransactionType(),
                        transactionCreatedEvent.getOperatorType(),
                        transactionCreatedEvent.getDescription()));
            }
            System.out.println("------" + isExist + "-------message receive:"+ transactionCreatedEvent.toString());
        }

        private void balanceUpdateRecordHandler(JsonNode jsonNode) throws JsonProcessingException {
            boolean isExist = true;
            ObjectMapper mapper = new ObjectMapper();
            BalanceUpdatedEvent balanceUpdatedEvent = mapper.treeToValue(jsonNode,BalanceUpdatedEvent.class);
            isExist = materializedViewCompareService.readItem(balanceUpdatedEvent);
            long updateTime = 0;

            if(!isExist) {
                materializedViewCompareService.createOrderDetail(new BalanceUpdatedEvent(balanceUpdatedEvent.getId(),
                        balanceUpdatedEvent.getTransactionAmount(),
                        balanceUpdatedEvent.getWalletId(),
                        balanceUpdatedEvent.getUpdateTime(),
                        balanceUpdatedEvent.getBalance(),
                        balanceUpdatedEvent.getBalance().subtract(balanceUpdatedEvent.getTransactionAmount())));

                updateTime = materializedViewCompareService.getLastupdateTime(balanceUpdatedEvent);
                //如果 UpdateTime 比 MView 上的紀錄還新 就需要更新 balance
                if (updateTime == 0 || balanceUpdatedEvent.getUpdateTime() > updateTime) {
                    materializedViewCompareService.updateBalance(new Balance(balanceUpdatedEvent.getWalletId(),
                            balanceUpdatedEvent.getWalletId(),
                            balanceUpdatedEvent.getBalance(),
                            balanceUpdatedEvent.getUpdateTime()));
                }
            }
            System.out.println("------" + isExist + "-------message receive:"+ balanceUpdatedEvent.toString());
        }

        //@Override
        /*public void run() {
            int counter = 0;
            while(true){
                //拉取消息，暂时不确认消息
                //kafkaConsumer.poll(0);
                counter += 1;
                if(counter == 2) {
                    kafkaConsumer.seekToBeginning(kafkaConsumer.assignment());
                }
                ConsumerRecords<Integer,String> consumerRecord=kafkaConsumer.poll(Duration.ofMillis(5000));
                for(ConsumerRecord record:consumerRecord){
                    System.out.println("-------------message receive:" + record.value());
                    kafkaConsumer.commitSync();
                }
            }
        }*/

        public static void main(String[] args) {
            //new Thread(new CheckTransactionRecordFunction("test")).start();
        }

}
