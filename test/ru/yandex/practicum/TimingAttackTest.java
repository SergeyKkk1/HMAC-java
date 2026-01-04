package ru.yandex.practicum;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.config.ConfigStorage;
import ru.yandex.practicum.service.ServiceHMAC;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimingAttackTest {

    private ServerHMAC serverHMAC;
    private ServiceHMAC serviceHMAC;

    @BeforeEach
    public void setUp() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        var log = new PrintWriter(System.out, true);
        ConfigStorage configStorage = new ConfigStorage(log);
        ConfigStorage.ConfigHMAC config = configStorage.load();
        serverHMAC = new ServerHMAC(configStorage, log);
        serviceHMAC = new ServiceHMAC(config);
        serverHMAC.run();
    }

    @AfterEach
    public void tearDown() {
        serverHMAC.stop();
    }

    @Test
    public void testVerifyIsConstantTime() {
        byte[] msg = new byte[1024];
        new Random().nextBytes(msg);
        byte[] validSig = "o4H7DI15BnTB6y6imiRMAiPTKMqyaYZPSvSPSby2ZLc=".getBytes(StandardCharsets.UTF_8);
        byte[] fastFailSig = Arrays.copyOf(validSig, validSig.length);
        fastFailSig[0] ^= 1;
        byte[] slowFailSig = Arrays.copyOf(validSig, validSig.length);
        slowFailSig[31] ^= 1;

        long fastFailTotalTime = 0;
        long slowFailTotalTime = 0;
        int iterations = 100_000;
        for (int i = 0; i < iterations; i++) {
            long start1 = System.nanoTime();
            serviceHMAC.verify(msg, fastFailSig);
            fastFailTotalTime += (System.nanoTime() - start1);

            long start2 = System.nanoTime();
            serviceHMAC.verify(msg, slowFailSig);
            slowFailTotalTime += (System.nanoTime() - start2);
        }

        double fastAvg = (double) fastFailTotalTime / iterations;
        double slowAvg = (double) slowFailTotalTime / iterations;
        double difference = Math.abs(fastAvg - slowAvg);
        double threshold = 0.05 * slowAvg;
        assertTrue(difference < threshold,
                "Possible Timing Attack Vulnerability! Large difference between fast and slow rejection.");
    }
}
