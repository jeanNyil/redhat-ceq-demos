package io.jeannyil.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonInclude(JsonInclude.Include.NON_NULL) // Ensure no null fields are serialized
@JsonPropertyOrder({ "fileName", "content" }) // Specify property order in JSON
@RegisterForReflection // Register for Quarkus reflection
public class ResponseMessage {

    private final String fileName;
    private final String content;

    // JsonCreator ensures all fields are required during deserialization
    @JsonCreator
    public ResponseMessage(
        @JsonProperty(value = "fileName", required = true) String fileName,
        @JsonProperty(value = "content", required = true) String content
    ) {
        this.fileName = fileName;
        this.content = content;
    }

    // Getters
    @JsonProperty("fileName")
    public String getFileName() {
        return fileName;
    }

    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    // toString() for easier logging or debugging
    @Override
    public String toString() {
        return "ResponseMessage{" +
                "fileName='" + fileName + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
