package io.jeannyil.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonInclude(JsonInclude.Include.NON_NULL) // Ensure no null fields are serialized
@JsonPropertyOrder({ "code", "description", "message" }) // Specify property order in JSON
@RegisterForReflection // Ensure the class is available for Quarkus reflection at runtime
public class ErrorMessage {

    private final String code;
    private final String description;
    private final String message;

    // JsonCreator ensures all fields are required during deserialization
    @JsonCreator
    public ErrorMessage(
        @JsonProperty(value = "code", required = true) String code,
        @JsonProperty(value = "description", required = true) String description,
        @JsonProperty(value = "message", required = true) String message
    ) {
        this.code = code;
        this.description = description;
        this.message = message;
    }

    // Getters
    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    // toString() for easier logging or debugging
    @Override
    public String toString() {
        return "ErrorMessage{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
