package com.secondserving.secondserving;

import com.secondserving.secondserving.TestUtils.PostgisTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgisTestContainerConfig.class)
class SecondservingApplicationTests {

	@Test
	void contextLoads() {
	}

}
