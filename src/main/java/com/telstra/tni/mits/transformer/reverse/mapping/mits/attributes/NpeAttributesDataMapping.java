package com.telstra.tni.mits.transformer.reverse.mapping.mits.attributes;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "npe.attributes")
public class NpeAttributesDataMapping {
    
    private Map<String, Object> miOtherNpeAttrs;
    
    public Map<String, Object> getMiOtherNpeAttrs() {
        return miOtherNpeAttrs;
    }
    
    public void setMiOtherNpeAttrs(Map<String, Object> miOtherNpeAttrs) {
        this.miOtherNpeAttrs = miOtherNpeAttrs;
    }
}