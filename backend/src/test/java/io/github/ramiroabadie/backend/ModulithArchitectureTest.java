package io.github.ramiroabadie.backend;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithArchitectureTest {

	@Test
	void verificaLimitesDeModulos() {
		ApplicationModules.of(BackendApplication.class).verify();
	}

}
