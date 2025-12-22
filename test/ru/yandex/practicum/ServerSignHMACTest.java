package ru.yandex.practicum;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.config.ConfigStorage;
import ru.yandex.practicum.config.HelperBase64;
import ru.yandex.practicum.service.ServiceHMAC;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerSignHMACTest extends BaseServerTest {
    private static final String TEST_MESSAGE = "Int test message";

    private ServerHMAC serverHMAC;
    private ServiceHMAC serviceHMAC;
    private ConfigStorage.ConfigHMAC config;

    @BeforeEach
    public void setUp() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        var log = new PrintWriter(System.out, true);
        config = new ConfigStorage(log).load();
        serverHMAC = new ServerHMAC(config, log);
        serviceHMAC = new ServiceHMAC(config, log);
        serverHMAC.run();
    }

    @AfterEach
    public void tearDown() {
        serverHMAC.stop();
    }

    @Test
    public void testCreateHmacAndVerify() {
        HandlerSignHMAC.SignRequest signRequest = new HandlerSignHMAC.SignRequest(TEST_MESSAGE);
        String jsonSingRequest = GSON.toJson(signRequest);

        HttpResponse<String> response = post("/sign", jsonSingRequest);
        assertEquals(200, response.statusCode());
        HandlerSignHMAC.SignResponse signResponse = GSON.fromJson(response.body(), HandlerSignHMAC.SignResponse.class);

        HandlerVerifyHMAC.VerifyRequest verifyRequest = new HandlerVerifyHMAC.VerifyRequest(TEST_MESSAGE, signResponse.getSignature());
        String jsonVerifyRequest = GSON.toJson(verifyRequest);
        HttpResponse<String> verifyResponseHttp = post("/verify", jsonVerifyRequest);

        assertEquals(200, verifyResponseHttp.statusCode());
        HandlerVerifyHMAC.VerifyResponse verifyResponse = GSON.fromJson(verifyResponseHttp.body(), HandlerVerifyHMAC.VerifyResponse.class);
        assertTrue(verifyResponse.isOk());
    }

    @Test
    public void testCreateHmacAndVerifyUnsuccessful_wrongSignature() {
        HandlerSignHMAC.SignRequest signRequest = new HandlerSignHMAC.SignRequest(TEST_MESSAGE);
        String jsonSingRequest = GSON.toJson(signRequest);

        HttpResponse<String> response = post("/sign", jsonSingRequest);
        assertEquals(200, response.statusCode());
        HandlerSignHMAC.SignResponse signResponse = GSON.fromJson(response.body(), HandlerSignHMAC.SignResponse.class);

        HandlerVerifyHMAC.VerifyRequest verifyRequest = new HandlerVerifyHMAC.VerifyRequest(TEST_MESSAGE, "A" + signResponse.getSignature().substring(1));
        String jsonVerifyRequest = GSON.toJson(verifyRequest);
        HttpResponse<String> verifyResponseHttp = post("/verify", jsonVerifyRequest);

        assertEquals(200, verifyResponseHttp.statusCode());
        HandlerVerifyHMAC.VerifyResponse verifyResponse = GSON.fromJson(verifyResponseHttp.body(), HandlerVerifyHMAC.VerifyResponse.class);
        assertFalse(verifyResponse.isOk());
    }

    @Test
    public void testCreateHmacAndVerifyUnsuccessful_wrongMessage() {
        HandlerSignHMAC.SignRequest signRequest = new HandlerSignHMAC.SignRequest(TEST_MESSAGE);
        String jsonSingRequest = GSON.toJson(signRequest);

        HttpResponse<String> response = post("/sign", jsonSingRequest);
        assertEquals(200, response.statusCode());
        HandlerSignHMAC.SignResponse signResponse = GSON.fromJson(response.body(), HandlerSignHMAC.SignResponse.class);

        HandlerVerifyHMAC.VerifyRequest verifyRequest = new HandlerVerifyHMAC.VerifyRequest(TEST_MESSAGE.substring(1), signResponse.getSignature());
        String jsonVerifyRequest = GSON.toJson(verifyRequest);
        HttpResponse<String> verifyResponseHttp = post("/verify", jsonVerifyRequest);

        assertEquals(200, verifyResponseHttp.statusCode());
        HandlerVerifyHMAC.VerifyResponse verifyResponse = GSON.fromJson(verifyResponseHttp.body(), HandlerVerifyHMAC.VerifyResponse.class);
        assertFalse(verifyResponse.isOk());
    }

    @Test
    public void testCreateHMAC() {
        HandlerSignHMAC.SignRequest signRequest = new HandlerSignHMAC.SignRequest(TEST_MESSAGE);
        String jsonSingRequest = GSON.toJson(signRequest);

        HttpResponse<String> response = post("/sign", jsonSingRequest);

        assertEquals(200, response.statusCode());
        HandlerSignHMAC.SignResponse signResponse = GSON.fromJson(response.body(), HandlerSignHMAC.SignResponse.class);
        byte[] actualSignatureBytes = HelperBase64.decode(signResponse.getSignature());
        assertTrue(serviceHMAC.verify(signRequest.getMsg().getBytes(StandardCharsets.UTF_8), actualSignatureBytes));
    }

    @Test
    public void testCreateHMAC_deterministic() {
        HandlerSignHMAC.SignRequest signRequest = new HandlerSignHMAC.SignRequest(TEST_MESSAGE);
        String jsonSingRequest = GSON.toJson(signRequest);

        HttpResponse<String> response1 = post("/sign", jsonSingRequest);
        HttpResponse<String> response2 = post("/sign", jsonSingRequest);

        assertEquals(200, response1.statusCode());
        assertEquals(200, response2.statusCode());
        HandlerSignHMAC.SignResponse signResponse1 = GSON.fromJson(response1.body(), HandlerSignHMAC.SignResponse.class);
        HandlerSignHMAC.SignResponse signResponse2 = GSON.fromJson(response2.body(), HandlerSignHMAC.SignResponse.class);
        assertEquals(signResponse1.getSignature(), signResponse2.getSignature());
    }

    @Test
    public void testCreateHMAC_invalidMsg() {
        HandlerSignHMAC.SignRequest signRequest = new HandlerSignHMAC.SignRequest("");
        String jsonSingRequest = GSON.toJson(signRequest);

        HttpResponse<String> response = post("/sign", jsonSingRequest);

        assertEquals(400, response.statusCode());
        ErrorMessageDto errorMessageDto = GSON.fromJson(response.body(), ErrorMessageDto.class);
        assertEquals("invalid_json", errorMessageDto.error());
    }

    @Test
    public void testCreateHMAC_tooLongMessage() {
        config.setMaxMsgSizeBytes(1);
        HandlerSignHMAC.SignRequest signRequest = new HandlerSignHMAC.SignRequest(TEST_MESSAGE);
        String jsonSingRequest = GSON.toJson(signRequest);

        HttpResponse<String> response = post("/sign", jsonSingRequest);

        assertEquals(413, response.statusCode());
        ErrorMessageDto errorMessageDto = GSON.fromJson(response.body(), ErrorMessageDto.class);
        assertEquals("invalid_msg", errorMessageDto.error());
    }

    @Test
    public void testCreateHMAC_unsupportedMediaType() throws IOException, InterruptedException {
        HandlerSignHMAC.SignRequest signRequest = new HandlerSignHMAC.SignRequest(TEST_MESSAGE);
        String jsonSingRequest = GSON.toJson(signRequest);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://localhost:8080%s", "/sign")))
                .header("Content-Type", "application/xml")
                .POST(HttpRequest.BodyPublishers.ofString(jsonSingRequest))
                .build();
        HttpResponse<String> response = CLIENT.send(postRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(415, response.statusCode());
        ErrorMessageDto errorMessageDto = GSON.fromJson(response.body(), ErrorMessageDto.class);
        assertEquals("invalid_json", errorMessageDto.error());
    }

    @Test
    public void testCreateHMAC_rotatedSecret() {
        String oldSecret = config.getSecret();
        String userInput = "y" + System.lineSeparator() + "exit" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(userInput.getBytes()));
        serverHMAC.runCommandLoop();
        String newSecret = config.getSecret();

        assertNotEquals(oldSecret, newSecret);

        HandlerSignHMAC.SignRequest signRequest = new HandlerSignHMAC.SignRequest(TEST_MESSAGE);
        String jsonSingRequest = GSON.toJson(signRequest);

        HttpResponse<String> response = post("/sign", jsonSingRequest);
        assertEquals(200, response.statusCode());
        HandlerSignHMAC.SignResponse signResponse = GSON.fromJson(response.body(), HandlerSignHMAC.SignResponse.class);

        HandlerVerifyHMAC.VerifyRequest verifyRequest = new HandlerVerifyHMAC.VerifyRequest(TEST_MESSAGE, signResponse.getSignature());
        String jsonVerifyRequest = GSON.toJson(verifyRequest);
        HttpResponse<String> verifyResponseHttp = post("/verify", jsonVerifyRequest);

        assertEquals(200, verifyResponseHttp.statusCode());
        HandlerVerifyHMAC.VerifyResponse verifyResponse = GSON.fromJson(verifyResponseHttp.body(), HandlerVerifyHMAC.VerifyResponse.class);
        assertTrue(verifyResponse.isOk());
    }
}
