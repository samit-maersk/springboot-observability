package net.samitkumar.springbootobservability;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest
class SpringbootObservabilityApplicationTests {

	@Test
	void contextLoads() {
	}

}

@ExtendWith(SpringExtension.class)
class SimpleTest {
	@Test
	void flixZipTest() {
		Mono number = Flux.just(1,2,3,4,5).collectList();
		Mono string = Flux.just("a","b","c","d").collectList();
		Mono
				.zip(number,string,NumberString::new)
				.subscribe((e) -> System.out.println(e));
	}
}

record NumberString(List<Integer> numbers, List<String> strings) {}