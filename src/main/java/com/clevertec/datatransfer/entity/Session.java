package com.clevertec.datatransfer.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Session {
    @JsonProperty("eid")
    private String sid;
    private User user;

    @Data
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        @JsonProperty("nm")
        private String name;
        @JsonProperty("id")
        private String userId;
    }
}
