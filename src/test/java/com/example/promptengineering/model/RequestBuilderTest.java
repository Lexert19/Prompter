package com.example.promptengineering.model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class RequestBuilderTest {

    private static final Logger log = LoggerFactory.getLogger(RequestBuilderTest.class);

    private RequestBuilder builder;
    private TextContent textContent;
    private Message userMessage;

    private Content createTextContent(String text) {
        TextContent content = new TextContent();
        content.setType("text");
        content.setText(text);
        return content;
    }

    @BeforeEach
    void setUp() {
        builder = new RequestBuilder();
        textContent = new TextContent();
        textContent.setType("text");
        textContent.setText("Hello, world!");
        userMessage = new Message("user", List.of(textContent));
    }

    @Test
    void shouldBuildOpenAiRequestWithSystemMessage() {
        builder.model("gpt-4")
                .addMessage(userMessage)
                .maxTokens(200)
                .temperature(0.7)
                .stream(true);
        builder.setProvider("OPENAI");
        builder.setSystem("You are a helpful assistant.");

        Map<String, Object> request = builder.build();

        log.info("OpenAI request with system: {}", request);

        assertThat(request)
                .containsEntry("model", "gpt-4")
                .containsEntry("max_tokens", 200)
                .containsEntry("temperature", 0.7)
                .containsEntry("stream", true)
                .containsKey("messages");

        List<Map<String, Object>> messages = (List<Map<String, Object>>) request.get("messages");
        assertThat(messages).hasSize(2);

        Map<String, Object> systemMsg = messages.get(0);
        assertThat(systemMsg).containsEntry("role", "system");
        assertThat(systemMsg.get("content")).isEqualTo(List.of(
                Map.of(
                        "type", "text",
                        "text", "You are a helpful assistant."
                )
        ));

        Map<String, Object> userMsg = messages.get(1);
        assertThat(userMsg).containsEntry("role", "user");
        assertThat(((List<Map<String, Object>>) userMsg.get("content")).get(0))
                .containsEntry("text", "Hello, world!");
    }

    @Test
    void shouldBuildAnthropicRequestWithSystemField() {
        builder.model("claude-3-opus-20240229")
                .addMessage(userMessage)
                .maxTokens(1000)
                .temperature(0.0)
                .stream(false);
        builder.setProvider("ANTHROPIC");
        builder.setSystem("You are Claude, a helpful AI.");

        Map<String, Object> request = builder.build();

        log.info("Anthropic request with system: {}", request);

        assertThat(request)
                .containsEntry("model", "claude-3-opus-20240229")
                .containsEntry("max_tokens", 1000)
                .containsEntry("temperature", 0.0)
                .containsEntry("stream", false)
                .containsEntry("system", "You are Claude, a helpful AI.");

        List<Map<String, Object>> messages = (List<Map<String, Object>>) request.get("messages");
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).containsEntry("role", "user");
    }

    @Test
    void shouldHandleImageContentForAnthropic() {
        ImageContent imageContent = new ImageContent();
        imageContent.setType("image");
        imageContent.setMediaType("image/png");
        imageContent.setData("base64EncodedImageData");

        Message messageWithImage = new Message("user", List.of(textContent, imageContent));

        builder.model("claude-3-opus-20240229")
                .addMessage(messageWithImage)
                .maxTokens(500);
        builder.setProvider("ANTHROPIC");

        Map<String, Object> request = builder.build();
        log.info("Anthropic request with image: {}", request);

        List<Map<String, Object>> messages = (List<Map<String, Object>>) request.get("messages");
        Map<String, Object> userMsg = messages.get(0);
        List<Map<String, Object>> contentList = (List<Map<String, Object>>) userMsg.get("content");
        assertThat(contentList).hasSize(2);

        Map<String, Object> imagePart = contentList.get(1);
        assertThat(imagePart).containsEntry("type", "image");

        Map<String, Object> source = (Map<String, Object>) imagePart.get("source");
        assertThat(source)
                .containsEntry("type", "base64")
                .containsEntry("media_type", "image/png")
                .containsEntry("data", "base64EncodedImageData");
    }

    @Test
    void shouldHandleImageContentForOpenAi() {
        ImageContent imageContent = new ImageContent();
        imageContent.setType("image");
        imageContent.setMediaType("image/jpeg");
        imageContent.setData("base64ImageData");

        Message messageWithImage = new Message("user", List.of(textContent, imageContent));

        builder.model("gpt-4-vision-preview")
                .addMessage(messageWithImage)
                .maxTokens(300);
        builder.setProvider("OPENAI");

        Map<String, Object> request = builder.build();
        log.info("OpenAI request with image: {}", request);

        List<Map<String, Object>> messages = (List<Map<String, Object>>) request.get("messages");
        Map<String, Object> userMsg = messages.get(0);
        List<Map<String, Object>> contentList = (List<Map<String, Object>>) userMsg.get("content");
        assertThat(contentList).hasSize(2);

        Map<String, Object> imagePart = contentList.get(1);
        assertThat(imagePart).containsEntry("type", "image_url");

        Map<String, String> imageUrl = (Map<String, String>) imagePart.get("image_url");
        assertThat(imageUrl).containsKey("url");
        assertThat(imageUrl.get("url")).startsWith("data:image/jpeg;base64,base64ImageData");
    }

    @Test
    void shouldApplyCacheControlForAnthropicWhenCacheTrue() {
        TextContent cachedContent = new TextContent();
        cachedContent.setType("text");
        cachedContent.setText("This is cached text");

        Message cachedMessage = new Message("user", List.of(cachedContent));
        cachedMessage.setCached(true);

        builder.model("claude-3-opus-20240229")
                .addMessage(cachedMessage)
                .maxTokens(100);
        builder.setProvider("ANTHROPIC");

        Map<String, Object> request = builder.build();
        log.info("Anthropic request with cache: {}", request);

        List<Map<String, Object>> messages = (List<Map<String, Object>>) request.get("messages");
        List<Map<String, Object>> contentList = (List<Map<String, Object>>) messages.get(0).get("content");
        Map<String, Object> firstContent = contentList.get(0);
        assertThat(firstContent).containsKey("cache_control");

        Map<String, String> cacheControl = (Map<String, String>) firstContent.get("cache_control");
        assertThat(cacheControl).containsEntry("type", "ephemeral");
    }

    @Test
    void shouldIncludeReasoningEffortForOpenAi() {
        builder.model("o1-preview")
                .addMessage(userMessage)
                .maxTokens(500);
        builder.setProvider("OPENAI");
        builder.setReasoningEffort("medium");

        Map<String, Object> request = builder.build();

        log.info("OpenAI request with reasoning effort: {}", request);

        assertThat(request).containsEntry("reasoning_effort", "medium");
        assertThat(request).containsKey("response_format");
        assertThat(request.get("response_format")).isEqualTo(Map.of("type", "text"));
    }

    @Test
    void shouldFallbackToEmptyContentIfMessageHasNoContent() {
        Message emptyMessage = new Message("user", null);
        builder.model("gpt-4")
                .addMessage(emptyMessage)
                .maxTokens(100);
        builder.setProvider("OPENAI");

        Map<String, Object> request = builder.build();

        log.info("Request with empty content: {}", request);

        List<Map<String, Object>> messages = (List<Map<String, Object>>) request.get("messages");
        List<Map<String, Object>> contentList = (List<Map<String, Object>>) messages.get(0).get("content");
        assertThat(contentList).hasSize(0);
        //assertThat(contentList.get(0)).containsEntry("text", "error");
    }

    @Test
    void shouldHandleAssistantMessage() {
        TextContent assistantContent = new TextContent();
        assistantContent.setType("text");
        assistantContent.setText("I am an assistant.");
        Message assistantMsg = new Message("assistant", List.of(assistantContent));

        builder.model("gpt-4").addMessage(assistantMsg);
        builder.setProvider("OPENAI");

        Map<String, Object> request = builder.build();
        List<Map<String, Object>> messages = (List<Map<String, Object>>) request.get("messages");
        assertThat(messages.get(0)).containsEntry("role", "assistant");
    }

    @Test
    void shouldHandleEmptyMessagesList() {
        builder.model("gpt-4").maxTokens(100);
        builder.setProvider("OPENAI");

        Map<String, Object> request = builder.build();
        List<Map<String, Object>> messages = (List<Map<String, Object>>) request.get("messages");
        assertThat(messages).isEmpty();
    }

    @Test
    void shouldPreserveMessageOrder() {
        Message msg1 = new Message("user", List.of(createTextContent("First")));
        Message msg2 = new Message("assistant", List.of(createTextContent("Second")));
        Message msg3 = new Message("user", List.of(createTextContent("Third")));

        builder.model("gpt-4").addMessage(msg1).addMessage(msg2).addMessage(msg3);
        builder.setProvider("OPENAI");

        Map<String, Object> request = builder.build();
        List<Map<String, Object>> messages = (List<Map<String, Object>>) request.get("messages");
        assertThat(messages).hasSize(3);
        assertThat(((List<Map<String, Object>>) messages.get(0).get("content")).get(0))
                .containsEntry("text", "First");
        assertThat(((List<Map<String, Object>>) messages.get(1).get("content")).get(0))
                .containsEntry("text", "Second");
        assertThat(((List<Map<String, Object>>) messages.get(2).get("content")).get(0))
                .containsEntry("text", "Third");
    }

    @Test
    void shouldUseDefaultMaxTokensAndTemperature() {
        builder.model("gpt-4").addMessage(userMessage);
        builder.setProvider("OPENAI");

        Map<String, Object> request = builder.build();
        assertThat(request).containsEntry("max_tokens", 16000);
        assertThat(request).containsEntry("temperature", 0.0);
    }

    @Test
    void shouldSetStreamToFalse() {
        builder.model("gpt-4").addMessage(userMessage).stream(false);
        builder.setProvider("OPENAI");

        Map<String, Object> request = builder.build();
        assertThat(request).containsEntry("stream", false);
    }

    @Test
    void shouldNotAddSystemMessageWhenSystemIsNull() {
        builder.model("gpt-4").addMessage(userMessage);
        builder.setProvider("OPENAI");
        builder.setSystem(null);

        Map<String, Object> request = builder.build();
        List<Map<String, Object>> messages = (List<Map<String, Object>>) request.get("messages");
        assertThat(messages).hasSize(1);
    }

//    @Test
//    void shouldThrowExceptionForUnknownContentType() {
//        TextContent badContent = new TextContent();
//        badContent.setType("audio");
//        badContent.setText("whatever");
//        Message msg = new Message("user", List.of(badContent));
//
//        builder.model("gpt-4").addMessage(msg);
//        builder.setProvider("OPENAI");
//
//        assertThatThrownBy(() -> builder.build())
//                .isInstanceOf(IllegalStateException.class)
//                .hasMessageContaining("Unknown content type");
//    }
}
