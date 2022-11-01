package com.example.digitaltwin.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateRequest {
    public final String name;

    @JsonCreator
    public CreateRequest(@JsonProperty String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
