package com.ckxnhat.truyenhay;

import com.ckxnhat.truyenhay.exception.AppException;
import com.ckxnhat.truyenhay.plugin.PluginManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class TruyenhayApplicationTests {
	PluginManager pluginManager = new PluginManager();
	@Test
	void testGetInvalidServerPlugin() {
		assertThrows(AppException.class, () -> {
			pluginManager.getServerPlugin("invalid-server");
		});
	}

}
