package com.dht.pags.wallet.materalizedviewprocesor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
public class WalletMaterializedViewProcessorTests {

	@Autowired
	private InputDestination inputDestination;

	@Autowired
	private OutputDestination outputDestination;

	@Test
	public void testWalletTransactionToCosmoDbView()
	{

	}


}
