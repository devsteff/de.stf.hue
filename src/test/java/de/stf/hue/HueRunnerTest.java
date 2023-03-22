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

    private void sleepaWhile(long time) {
        try {
            Thread.sleep(time);
        } catch(InterruptedException ex) {}
    }

    @Test
    @Order(1)
    void testNoOne() {
        sleepaWhile(1000);
        assertNotNull(w.getHUE());
    }

    @Test
    @Order(2)
    void testNoTwo() {
        sleepaWhile(1000);
        assertNotNull(w.getHUE());
    }

    @ParameterizedTest(name = "{index} => valueToTest={0}")
    @MethodSource("intProvider")
    @DisplayName("failingTest 😱")
    @Order(3)
    void failingTest(Integer valueToTest) {
        sleepaWhile(1000);
        try {
            assertTrue(valueToTest != 9);
        } catch (Throwable ex) {
            if(valueToTest==9) w.main(null);
            throw ex;
        }
    }
}