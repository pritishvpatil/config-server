package com.telstra.tni.mits.transformer.reverse.mapping.mits.attributes;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "reverse-mapping")
public class NpeAttributesDataMapping {

    private List<String> columnMapping;
    private Map<String, Object> miOtherNpeAttrs;
    private String csvFilePath;
    private int skip = 0;
    private int top = 1000;

    // getters and setters

    public List<String> getColumnMapping() {
        return columnMapping;
    }

    public void setColumnMapping(List<String> columnMapping) {
        this.columnMapping = columnMapping;
    }

   

	public Map<String, Object> getMiOtherNpeAttrs() {
		return miOtherNpeAttrs;
	}

	public void setMiOtherNpeAttrs(Map<String, Object> miOtherNpeAttrs) {
		this.miOtherNpeAttrs = miOtherNpeAttrs;
	}

	public String getCsvFilePath() {
        return csvFilePath;
    }

    public void setCsvFilePath(String csvFilePath) {
        this.csvFilePath = csvFilePath;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }
}