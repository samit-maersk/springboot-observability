package net.samitkumar.springbootobservability;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SpringbootObservabilityApplicationTests {

	@Test
	void contextLoads() {
	}

}

@ExtendWith(SpringExtension.class)
class SimpleTest {
	@Test
	void fluxZipTest() {
		Mono number = Flux.just(1,2,3,4,5).collectList();
		Mono string = Flux.just("a","b","c","d").collectList();
		Mono
				.zip(number,string,NumberString::new)
				.subscribe((e) -> System.out.println(e));
	}
	@Test
	void nullCastTest() {
		assertEquals(null, (String)null);
		assertEquals(null, (Throwable)null);
	}
}

record NumberString(List<Integer> numbers, List<String> strings) {}