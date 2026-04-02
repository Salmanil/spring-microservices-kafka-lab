package com.example.employee_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@SpringBootTest
class EmployeeApiApplicationTests {

	@MockBean
	private RedisMessageListenerContainer redisMessageListenerContainer;

	@Test
	void contextLoads() {
	}

}
