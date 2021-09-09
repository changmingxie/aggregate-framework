package org.aggregateframework.transaction.server.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.Accessors;
import org.aggregateframework.ha.registry.Owner;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 注册信息
 */
@Value
@Accessors(fluent = true)
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Registration0.Builder.class)
@Deprecated
public class Registration0 implements Serializable {
    
    @JsonProperty                       String              application;
    @JsonProperty                       String              password;
    // by default, host to registry
    @JsonProperty                       String              host;
    // by default, port to registry
    @JsonProperty                       int                 port;
    @JsonProperty                       int                 database;
    @JsonProperty                       String              domain;
    @Getter(onMethod_ = @JsonAnyGetter) Map<String, Object> any;
    @JsonProperty @Singular             List<Owner>         owners;
    
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        
        
        @JsonAnySetter
        public Builder any(String anyKey, Object anyValue) {
            if (this.any == null) {
                this.any = new LinkedHashMap<>();
            }
            this.any.put(anyKey, anyValue);
            return this;
        }
        
    }
    
    
}
