package dev.nightowl.codexconnect.client;

import dev.nightowl.codexconnect.model.response.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class StreamingHandler {

    public static Consumer<StreamEvent> createCollectingHandler(
            Consumer<StreamEvent> onEvent,
            Consumer<ChatResponse> onComplete) {

        ResponseCollector collector = new ResponseCollector();

        return event -> {
            collector.handleEvent(event);
            if (onEvent != null) {
                onEvent.accept(event);
            }

            if (event.getType() != null &&
                    (event.getType().equals("response.done") ||
                            event.getType().equals("response.completed"))) {
                if (onComplete != null) {
                    onComplete.accept(collector.buildResponse());
                }
            }
        };
    }

    public interface StreamCallback {
        void onEvent(StreamEvent event);

        void onComplete(ChatResponse finalResponse);

        void onError(Exception e);
    }

    public static class ResponseCollector {
        private final List<String> contentParts = new ArrayList<>();
        private final Map<String, ToolCallBuilder> toolCallBuilders = new HashMap<>();
        private final ReasoningBuilder reasoningBuilder = new ReasoningBuilder();
        private String responseId;
        private String status;
        private ResponseTokenUsage responseTokenUsage;

        public void handleEvent(StreamEvent event) {
            if (event.getType() != null) {
                switch (event.getType()) {
                    case "response.created":
                    case "response.output_item.added":
                        if (event.getResponse() != null) {
                            responseId = event.getResponse().getId();
                        }
                        if (event.getType().equals("reasoning")) {
                            if (reasoningBuilder.id == null) {
                                System.out.println("setid: " + event.getItem().getId());
                                reasoningBuilder.id = event.getItem().getId();
                            }
                        }
                        if (event.getItem() != null && "function_call".equals(event.getItem().getType())) {
                            ToolCallBuilder builder = new ToolCallBuilder();
                            builder.id = event.getItem().getId();
                            toolCallBuilders.put(builder.id, builder);
                        }
                        break;

                    case "response.reasoning_summary_text.delta":

                        if (reasoningBuilder.reasoningTitle == null) {
                            String thisDelta = event.getDelta().getText();
                            //the first delta is allways the title of the thinking plus \n and some random part of the response
                            //**Creating a Java Haiku**
                            //
                            //I
                            //so we split after the first line so we only get the accual tittle,

                            String[] parts = thisDelta.split("\\r?\\n", 2);
                            if (parts.length > 0) {
                                reasoningBuilder.reasoningTitle = parts[0];
                                //we need to add the rest to reasoning parts
                                reasoningBuilder.reasoningParts.add(parts.length > 1 ? parts[1].replace("\\n", "") : ""); //we remove the \n to remove the enter that it sepearets the nmoram lthinking from the title itself to make the oputput cleaner!
                            }
                            return;
                        }
                        if (event.getDelta() != null && event.getDelta().getText() != null) {
                            reasoningBuilder.reasoningParts.add(event.getDelta().getText());
                        }
                        break;
                    case "response.content_part.delta":
                    case "response.output_item.content_part.delta":
                    case "response.output_item.delta":
                    case "response.output_text.delta":
                        if (event.getDelta() != null && event.getDelta().getText() != null) {
                            contentParts.add(event.getDelta().getText());
                        }
                        break;

                    case "response.function_call_arguments.delta":
                        if (event.getDelta() != null && event.getDelta().getText() != null) {
                            StreamEvent.Item item = event.getItem();
                            if (item != null && item.getId() != null) {
                                ToolCallBuilder builder = toolCallBuilders.get(item.getId());
                                if (builder != null) {
                                    builder.arguments.append(event.getDelta().getText());
                                }
                            } else {
                                String lastId = toolCallBuilders.keySet().stream()
                                        .reduce((first, second) -> second)
                                        .orElse(null);
                                if (lastId != null) {
                                    ToolCallBuilder builder = toolCallBuilders.get(lastId);
                                    if (builder != null) {
                                        builder.arguments.append(event.getDelta().getText());
                                    }
                                }
                            }
                        }
                        break;

                    case "response.output_item.done":
                        if (event.getItem() != null && "function_call".equals(event.getItem().getType())) {
                            ToolCallBuilder builder = toolCallBuilders.get(event.getItem().getId());
                            if (builder != null && event.getItem().getName() != null) {
                                builder.name = event.getItem().getName();
                            }
                        }
                        break;

                    case "response.done":
                    case "response.completed":
                        if (event.getResponse() != null) {
                            status = event.getResponse().getStatus();
                            responseTokenUsage = event.getResponse().getUsage();

                            if (event.getResponse().getOutput() != null) {
                                for (StreamEvent.Item item : event.getResponse().getOutput()) {
                                    if ("function_call".equals(item.getType())) {
                                        ToolCallBuilder builder = toolCallBuilders.get(item.getId());
                                        if (builder != null && item.getName() != null) {
                                            builder.name = item.getName();
                                        }
                                        if (builder != null && item.getArguments() != null) {
                                            builder.arguments = new StringBuilder(item.getArguments());
                                        }
                                    }
                                }
                            }
                        }
                        if (event.getUsage() != null) {
                            responseTokenUsage = event.getUsage();
                        }
                        break;
                }
            }
        }

        public ChatResponse buildResponse() {
            String fullContent = String.join("", contentParts);

            List<ChatResponse.MessageContent> messages = new ArrayList<>();
            if (!fullContent.isEmpty()) {
                messages.add(ChatResponse.MessageContent.builder()
                        .role("assistant")
                        .content(fullContent)
                        .build());
            }

            List<ToolCall> toolCalls = new ArrayList<>();
            for (ToolCallBuilder builder : toolCallBuilders.values()) {
                toolCalls.add(builder.build());
            }

            return ChatResponse.builder()
                    .id(responseId)
                    .status(status)
                    .reasoning(reasoningBuilder.build())
                    .messages(messages)
                    .toolCalls(toolCalls.isEmpty() ? null : toolCalls)
                    .usage(responseTokenUsage)
                    .build();
        }

        private static class ReasoningBuilder {
            private final List<String> reasoningParts = new ArrayList<>();
            String reasoningTitle;
            String id;

            public void addPart(String part) {
                reasoningParts.add(part);
            }

            public Reasoning build() {
                return Reasoning.builder()
                        .id(id)
                        .title(reasoningTitle)
                        .content(String.join("", reasoningParts))
                        .build();
            }
        }

        private static class ToolCallBuilder {
            String id;
            String name;
            StringBuilder arguments = new StringBuilder();

            ToolCall build() {
                return ToolCall.builder()
                        .id(id)
                        .callId(id)
                        .name(name)
                        .arguments(arguments.toString())
                        .build();
            }
        }
    }
}
