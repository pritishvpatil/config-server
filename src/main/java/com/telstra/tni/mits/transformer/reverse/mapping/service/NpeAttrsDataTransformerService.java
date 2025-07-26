package com.telstra.tni.mits.transformer.reverse.mapping.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.telstra.tni.mits.transformer.reverse.mapping.mits.attributes.NpeAttributesDataMapping;

@Service
public class NpeAttrsDataTransformerService {

    private final Map<String, Object> miOtherNpeAttrs;

    public NpeAttrsDataTransformerService(NpeAttributesDataMapping attributeDataMapping) {
        this.miOtherNpeAttrs = attributeDataMapping.getMiOtherNpeAttrs();
    }

    @SuppressWarnings("unchecked")
    public List<List<String>> transformItemToRows(JsonNode item, String objectType) {
        List<List<String>> rows = new ArrayList<>();
        Set<String> seenNpeIdTypeName = new HashSet<>();
        Object typeMapObj = miOtherNpeAttrs.get(objectType);

        if (!(typeMapObj instanceof Map)) {
            return rows;
        }

        Map<String, Object> propertyMap = (Map<String, Object>) typeMapObj;

        for (Map.Entry<String, Object> entry : propertyMap.entrySet()) {
            String jsonProp = entry.getKey();
            Object mappingValue = entry.getValue();

            // Case 1: Simple key-value pair
            if (mappingValue instanceof String) {
                JsonNode valueNode = item.get(jsonProp);
                if (valueNode != null && !valueNode.isNull() && !valueNode.asText().isEmpty()) {
                    String npeId = resolveNpeId(item, objectType);
                    if (npeId == null || npeId.isEmpty()) continue;

                    String typeName = (String) mappingValue;
                    String dedupKey = npeId + "|" + typeName;
                    if (!seenNpeIdTypeName.add(dedupKey)) continue;

                    List<String> row = new ArrayList<>();
                    row.add(npeId);
                    row.add(typeName);
                    row.add("N");
                    row.add("NONE");
                    rows.add(row);
                }

            // Case 2: Enum mapping (nested Map<String, String>)
            } else if (mappingValue instanceof Map) {
                JsonNode enumValueNode = item.get(jsonProp);
                if (enumValueNode != null && !enumValueNode.isNull() && !enumValueNode.asText().isEmpty()) {
                    String enumKey = enumValueNode.asText();

                    Map<?, ?> enumMapRaw = (Map<?, ?>) mappingValue;

                    // Use only string keys and values
                    if (enumMapRaw.keySet().stream().allMatch(k -> k instanceof String) &&
                        enumMapRaw.values().stream().allMatch(v -> v instanceof String)) {

                        Map<String, String> enumMap = (Map<String, String>) enumMapRaw;
                        String typeName = enumMap.get(enumKey);

                        if (typeName != null && !typeName.isEmpty()) {
                            String npeId = resolveNpeId(item, objectType);
                            if (npeId == null || npeId.isEmpty()) continue;

                            String dedupKey = npeId + "|" + typeName;
                            if (!seenNpeIdTypeName.add(dedupKey)) continue;

                            List<String> row = new ArrayList<>();
                            row.add(npeId);
                            row.add(typeName);
                            row.add("N");
                            row.add("NONE");
                            rows.add(row);
                        }
                    }
                }
            }
        }

        return rows;
    }


    private String resolveNpeId(JsonNode item, String objectType) {
        String assocKey = "_ASSOCIATED_" + objectType.toUpperCase() + "_TYPE";
        JsonNode associatedNode = item.get(assocKey);

        if (associatedNode != null && !associatedNode.isNull()) {
            JsonNode npeIdNode = associatedNode.get("npeId");
            if (npeIdNode != null && !npeIdNode.isNull() && !npeIdNode.asText().isEmpty()) {
                return npeIdNode.asText();
            }

            JsonNode idNode = associatedNode.get("id");
            if (idNode != null && !idNode.isNull() && !idNode.asText().isEmpty()) {
                return idNode.asText();
            }
        }

        // Fallback to _typeId from main item if above are null
        JsonNode typeIdNode = item.get("_typeId");
        if (typeIdNode != null && !typeIdNode.isNull() && !typeIdNode.asText().isEmpty()) {
            return typeIdNode.asText();
        }

        return null;
    }
}