package ru.yandex.practicum;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.config.ConfigStorage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.http.HttpResponse;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerVerifyHMACTest extends BaseServerTest {
    private static final String TEST_MESSAGE = "Int test message";

    private ServerHMAC serverHMAC;

    @BeforeEach
    public void setUp() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        var log = new PrintWriter(System.out, true);
        ConfigStorage configStorage = new ConfigStorage(log);
        serverHMAC = new ServerHMAC(configStorage, log);
        serverHMAC.run();
    }

    @AfterEach
    public void tearDown() {
        serverHMAC.stop();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Not base64 signature"})
    public void testVerifyUnsuccessful_signatureNotBase64(String signature) {
        VerifyRequest verifyRequest = new VerifyRequest(TEST_MESSAGE, signature);
        String jsonVerifyRequest = GSON.toJson(verifyRequest);

        HttpResponse<String> verifyResponseHttp = post("/verify", jsonVerifyRequest);

        assertEquals(400, verifyResponseHttp.statusCode());
        ErrorMessageDto errorMessageDto = GSON.fromJson(verifyResponseHttp.body(), ErrorMessageDto.class);
        assertEquals("invalid_signature_format", errorMessageDto.error());
    }

    @Test
    public void testVerifyHMAC_tooLongMessage() {
        serverHMAC.getConfigHMAC().setMaxMsgSizeBytes(1);
        VerifyRequest verifyRequest = new VerifyRequest(TEST_MESSAGE, "signature");
        String jsonVerifyRequest = GSON.toJson(verifyRequest);

        HttpResponse<String> response = post("/verify", jsonVerifyRequest);

        assertEquals(413, response.statusCode());
        ErrorMessageDto errorMessageDto = GSON.fromJson(response.body(), ErrorMessageDto.class);
        assertEquals("invalid_msg", errorMessageDto.error());
    }
}
