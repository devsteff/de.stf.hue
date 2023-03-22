package de.stf.hue;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HueRunnerTest {
    private static HueRunner w = null;

    @BeforeAll
    static void setup() {
        w = new HueRunner();
    }

    private static Stream<Integer> intProvider() {
        return Stream.of(0,1,815,2,-1,4,4711,5,10,9);
    }

    private void sleepAWhile() {
        try {
            Thread.sleep(1000);
        } catch(InterruptedException ignored) {}
    }

    @Test
    @Order(1)
    void testNoOne() {
        sleepAWhile();
        assertNotNull(w.HUE_SDK);
    }

    @Test
    @Order(2)
    void testNoTwo() {
        sleepAWhile();
        assertNotNull(w.HUE_SDK);
    }

    @ParameterizedTest(name = "{index} => valueToTest={0}")
    @MethodSource("intProvider")
    @DisplayName("failingTest 😱")
    @Order(3)
    void failingTest(Integer valueToTest) {
        sleepAWhile();
        try {
            assertTrue(valueToTest != 5);
        } catch (Throwable ex) {
            if(valueToTest==5) HueRunner.main(null);
        }
    }
}