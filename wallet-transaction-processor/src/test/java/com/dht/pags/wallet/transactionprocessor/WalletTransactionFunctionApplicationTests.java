package com.dht.pags.wallet.transactionprocessor;

import com.dht.pags.wallet.domain.*;
import com.dht.pags.wallet.transactionprocessor.event.BalanceUpdatedEventDeserializer;
import com.dht.pags.wallet.transactionprocessor.event.CreateTransactionCommandProcessedEventDeserializer;
import com.dht.pags.wallet.transactionprocessor.event.CreateTransactionCommandSerializer;
import com.dht.pags.wallet.transactionprocessor.event.TransactionCreatedEventDeserializer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
@DirtiesContext
@ActiveProfiles("test")
class WalletTransactionFunctionApplicationTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(WalletTransactionFunctionApplicationTests.class);
	private static final String IN_TOPIC = "wallet.createTransactionCommand";
	private static final String OUT_TOPIC = "wallet.createTransactionCommandProcessedEvent";
	private static final String SUCCESS_TRANSACTION_TOPIC_NAME ="wallet.transactionCreatedEvent";
	private static final String BALANCE_UPDATED_TOPIC_NAME="wallet.balanceUpdatedEvent";
	private static final String STORE_NAME="TransactionCreatedEventStore";

	@ClassRule
	private static EmbeddedKafkaRule embeddedKafkaRule = new EmbeddedKafkaRule(1, true,IN_TOPIC,OUT_TOPIC, SUCCESS_TRANSACTION_TOPIC_NAME,BALANCE_UPDATED_TOPIC_NAME);
	private static EmbeddedKafkaBroker embeddedKafka = embeddedKafkaRule.getEmbeddedKafka();

	private static Consumer<String, CreateTransactionCommandProcessedEvent> consumerProcessedEvent;
	private static Consumer<String, BalanceUpdatedEvent> consumerBalanceUpdatedEvent;
	private static Consumer<String, TransactionCreatedEvent> consumerTransactionCreatedEvent;
	private static KafkaTemplate<String, CreateTransactionCommand> producer;

	/**
	 * Consumer CreateTransactionCommandProcessedEvent
	 * @return
	 */
	private static Consumer<String, CreateTransactionCommandProcessedEvent> getConsumerProcessedEvent(){
		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("getConsumerProcessedEventGroup", "true",
				embeddedKafka);
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CreateTransactionCommandProcessedEventDeserializer.class);
		DefaultKafkaConsumerFactory<String, CreateTransactionCommandProcessedEvent> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
		return cf.createConsumer();
	}

	/**
	 * Consumer TransactionCreatedEvent
	 * @return
	 */
	private static Consumer<String, TransactionCreatedEvent> getConsumerTransactionCreatedEvent(){
		Map<String, Object> consumerProps2 = KafkaTestUtils.consumerProps("getConsumerTransactionCreatedEventGroup", "true",
				embeddedKafka);
		consumerProps2.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerProps2.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		consumerProps2.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, TransactionCreatedEventDeserializer.class);
		DefaultKafkaConsumerFactory<String, TransactionCreatedEvent> cf2 = new DefaultKafkaConsumerFactory<>(consumerProps2);
		return cf2.createConsumer();
	}

	/**
	 * Consumer BalanceUpdatedEvent
	 * @return
	 */
	private static Consumer<String, BalanceUpdatedEvent> getConsumerBalanceUpdatedEvent(){
		Map<String, Object> consumerProps2 = KafkaTestUtils.consumerProps("getConsumerBalanceUpdatedEventGroup", "true",
				embeddedKafka);
		consumerProps2.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerProps2.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		consumerProps2.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BalanceUpdatedEventDeserializer.class);
		DefaultKafkaConsumerFactory<String, BalanceUpdatedEvent> cf2 = new DefaultKafkaConsumerFactory<>(consumerProps2);
		return cf2.createConsumer();
	}

	/**
	 * producer ，in  傳送資料通道
	 * @return
	 */
	private static KafkaTemplate<String, CreateTransactionCommand>  getKafkaTemplate(){
		Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
		producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CreateTransactionCommandSerializer.class);
		DefaultKafkaProducerFactory<String, CreateTransactionCommand> pf = new DefaultKafkaProducerFactory<>(producerProps);
		return new KafkaTemplate<>(pf, true);
	}

	@BeforeAll
	public static void setUp() {
		embeddedKafka.afterPropertiesSet();
		consumerProcessedEvent = getConsumerProcessedEvent();
		consumerBalanceUpdatedEvent = getConsumerBalanceUpdatedEvent();
		consumerTransactionCreatedEvent = getConsumerTransactionCreatedEvent();
		producer = getKafkaTemplate();
	}

	@AfterAll
	public static void tearDown() {
		try {
			consumerProcessedEvent.close();
			consumerBalanceUpdatedEvent.close();
			consumerTransactionCreatedEvent.close();
			LOGGER.info("==================================embeddedKafka.destroy()=========================================");
			embeddedKafka.destroy();
		} catch (Exception e){
			LOGGER.info(e.getMessage());
		}
	}

	@Test
	public void sampleTest1() {
		//訂閱
		embeddedKafka.consumeFromAnEmbeddedTopic(consumerProcessedEvent, OUT_TOPIC);
		embeddedKafka.consumeFromAnEmbeddedTopic(consumerBalanceUpdatedEvent, BALANCE_UPDATED_TOPIC_NAME);
		embeddedKafka.consumeFromAnEmbeddedTopic(consumerTransactionCreatedEvent, SUCCESS_TRANSACTION_TOPIC_NAME);


		SpringApplication app = new SpringApplication(TransactionProcessorApplication.class);
		app.setWebApplicationType(WebApplicationType.NONE);
		try (ConfigurableApplicationContext context = app.run(
				"--spring.cloud.stream.bindings.createTransactionCommandHandler-in-0.destination=" + IN_TOPIC,
				"--spring.cloud.stream.bindings.createTransactionCommandHandler-out-0.destination=" + OUT_TOPIC,
				"--spring.cloud.stream.kafka.streams.binder.brokers=" + embeddedKafka.getBrokersAsString())) {
			String walletId = "E000IF53";
			String orderId = "TEST1234";
			for (int i =1 ; i < 10 ; i++){
				testEvent(new CreateTransactionCommand(orderId+i, new BigDecimal("100.01"),walletId,TransactionType.DEPOSIT,OperatorType.API_THIRD_PARTY_PAYMENT,"TEST DEPOSIT"));
			}//跑9次 查log , balance=900.09
		}
	}

//	@Test
//	public void sampleTest2() {
//		embeddedKafka.consumeFromAnEmbeddedTopic(consumerProcessedEvent, OUT_TOPIC);
//		embeddedKafka.consumeFromAnEmbeddedTopic(consumerBalanceUpdatedEvent, BALANCE_UPDATED_TOPIC_NAME);
//		embeddedKafka.consumeFromAnEmbeddedTopic(consumerTransactionCreatedEvent, TRANSACTION_TOPIC_NAME);
//		SpringApplication app = new SpringApplication(TransactionProcessorApplication.class);
//		app.setWebApplicationType(WebApplicationType.NONE);
//		try (ConfigurableApplicationContext context = app.run(
//				"--spring.cloud.stream.bindings.createTransactionCommandHandler-in-0.destination=" + IN_TOPIC,
//				"--spring.cloud.stream.bindings.createTransactionCommandHandler-out-0.destination=" + OUT_TOPIC,
//				"--spring.cloud.stream.kafka.streams.binder.brokers=" + embeddedKafka.getBrokersAsString())) {
//			String walletId = "E000IF54";
//			String orderId = "TEST1234";
//			for (int i =1 ; i < 10 ; i++){
//				testEvent(new CreateTransactionCommand(orderId+i, new BigDecimal("100.01"),walletId,TransactionType.DEPOSIT,"TEST DEPOSIT"));
//				if((i %2 )== 0){
//					testEvent(new CreateTransactionCommand(orderId+i, new BigDecimal("1"),walletId,TransactionType.WITHDRAW_APPLY,"TEST WITHDRAW_APPLY"));
//				}
//			}//
//
//		}
//	}
	private void  testEvent(CreateTransactionCommand command){
		String walletId = command.getWalletId();
		String orderId = command.getOrderId();
		producer.send(IN_TOPIC,walletId,command);

		ConsumerRecord<String, CreateTransactionCommandProcessedEvent> record = KafkaTestUtils.getSingleRecord(consumerProcessedEvent, OUT_TOPIC);

		//
		CreateTransactionCommandProcessedEvent processedEvent= record.value();
		LOGGER.info("=================================="+processedEvent);
		assertThat(processedEvent.getOrderId()).isEqualTo(orderId);
		String id = processedEvent.getId();

		//
		ConsumerRecord<String, TransactionCreatedEvent> recordTransactionCreatedEvent = KafkaTestUtils.getSingleRecord(consumerTransactionCreatedEvent, SUCCESS_TRANSACTION_TOPIC_NAME);
		TransactionCreatedEvent transactionCreatedEvent=recordTransactionCreatedEvent.value();
		LOGGER.info("=================================="+transactionCreatedEvent);
		assertThat(transactionCreatedEvent.getOrderId()).isEqualTo(orderId);

		//
		ConsumerRecord<String, BalanceUpdatedEvent> recordBalanceUpdatedEvent = KafkaTestUtils.getSingleRecord(consumerBalanceUpdatedEvent, BALANCE_UPDATED_TOPIC_NAME);
		BalanceUpdatedEvent balanceUpdatedEvent=recordBalanceUpdatedEvent.value();
		LOGGER.info("=================================="+balanceUpdatedEvent);
		assertThat(balanceUpdatedEvent.getId()).isEqualTo(id);
	}
}