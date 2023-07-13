package com.imss.sivimss.pagoanticipado;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PagoAnticipadoApplicationTests {

	@Test
	void contextLoads() {
		String result = "test";
		PagoAnticipadoApplication.main(new String[] {});
		assertNotNull(result);
	}

}
