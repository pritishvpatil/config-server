package com.telstra.tni.mits.transformer.reverse.mapping.processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jobrunr.jobs.context.JobContext;
import org.jobrunr.jobs.context.JobRunrDashboardLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.telstra.tni.commonutils.dto.EntityMetadata;
import com.telstra.tni.commonutils.records.ItemMetadata;
import com.telstra.tni.commonutils.service.InventoryApiWebClient;
import com.telstra.tni.mits.transformer.reverse.mapping.mits.attributes.NpeAttributesDataMapping;
import com.telstra.tni.mits.transformer.reverse.mapping.service.NpeAttrsDataTransformerService;

import reactor.core.publisher.Mono;

@Service
public class NpeAttrsDataHandlerImpl implements TNIMitsDataHandler {

    private static final Logger log = new JobRunrDashboardLogger(LoggerFactory.getLogger(NpeAttrsDataHandlerImpl.class));

    private final InventoryApiWebClient inventoryApiWebClient;
    private final NpeAttributesDataMapping attributeDataMapping;
    private final NpeAttrsDataTransformerService npeAttrsDataTransformerService;

    private String generatedCsvFullPath;

    public NpeAttrsDataHandlerImpl(InventoryApiWebClient inventoryApiWebClient,
    		NpeAttributesDataMapping attributeDataMapping,
    		NpeAttrsDataTransformerService npeAttrsDataTransformerService) {
        this.inventoryApiWebClient = inventoryApiWebClient;
        this.attributeDataMapping = attributeDataMapping;
        this.npeAttrsDataTransformerService = npeAttrsDataTransformerService;
    }

    @Override
    public void process(JobContext jobContext) throws IOException {
        // Extract object types from the mapping keys
        List<String> objectTypes = new ArrayList<>(attributeDataMapping.getMiOtherNpeAttrs().keySet());

        Map<String, Optional<JsonNode>> attrDataMap = extractObjectsMetaDataBatch(objectTypes);

        // Process metadata to CSV
        processObjectAttrsDataToCsv(attrDataMap, attributeDataMapping, "MI_ALLOWABLE_ATTRS.csv");
    }

    private void processObjectAttrsDataToCsv(Map<String, Optional<JsonNode>> dataMap,
    		NpeAttributesDataMapping attributeDataMapping,
                                      String fileName) throws IOException {

        String filePath = attributeDataMapping.getCsvFilePath();

        List<String> csvHeaders = new ArrayList<>(attributeDataMapping.getColumnMapping());
        List<List<String>> csvRows = new ArrayList<>();

        for (Map.Entry<String, Optional<JsonNode>> entry : dataMap.entrySet()) {
            String objectType = entry.getKey();
            Optional<JsonNode> optionalJsonNode = entry.getValue();

            if (!optionalJsonNode.isPresent()) {
                log.warn("No data found for object type: {}", objectType);
                continue;
            }

            JsonNode objectNode = optionalJsonNode.get();

            if (!objectNode.isArray()) {
                log.warn("Expected array for object type '{}', but got: {}", objectType, objectNode);
                continue;
            }

            for (JsonNode item : objectNode) {
                List<List<String>> rows = npeAttrsDataTransformerService.transformItemToRows(item, objectType);
                for (List<String> row : rows) {
                    // Add row only if it has some non-empty value
                    if (row.stream().anyMatch(val -> !val.isEmpty())) {
                        csvRows.add(row);
                    }
                }
            }

        }

        writeCsv(filePath, fileName, csvHeaders, csvRows);
        this.generatedCsvFullPath = Paths.get(filePath, fileName).toString();
    }

    @Override
    public String getGeneratedCsvFilePath() {
        return this.generatedCsvFullPath;
    }

    public Map<String, Optional<JsonNode>> extractObjectsMetaDataBatch(List<String> objectTypes) {
        log.info("Starting extraction of TNI items in batches...");

        Map<String, Optional<JsonNode>> resultMap = objectTypes.stream()
                .collect(Collectors.toMap(
                        objectType -> objectType,
                        this::fetchAllBatchesForObject
                ));

        log.info("Finished extraction.");
        return resultMap;
    }

    private Optional<JsonNode> fetchAllBatchesForObject(String objectType) {
        boolean hasMore = true;
        int skip = attributeDataMapping.getSkip();
        int top = attributeDataMapping.getTop();

        ArrayNode aggregatedResults = JsonNodeFactory.instance.arrayNode();

        try {
            while (hasMore) {
                MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
                queryParams.add("$skip", String.valueOf(skip));
                queryParams.add("$top", String.valueOf(top));
                
                 // Add $expand if HAS_AUDIT exists for this objectType
                ItemMetadata itemMetadata = EntityMetadata.getEntityTypesMap().get(objectType);
                String associatedType = "_ASSOCIATED_" + objectType.toUpperCase() + "_TYPE";
                if (itemMetadata != null && itemMetadata.multiplicity().containsKey(associatedType)) {
                    queryParams.add("$expand", associatedType);
                }

                log.info("Fetching TNI data for {} with $skip={}, $top={}", objectType, skip, top);

                Mono<ResponseEntity<JsonNode>> itemMono = inventoryApiWebClient.getItemWithHttpInfo(objectType, queryParams);
                ResponseEntity<JsonNode> response = itemMono.block();

                if (response == null || !response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    log.warn("No response or unsuccessful status for object type: {}", objectType);
                    break;
                }

                JsonNode body = response.getBody();
                JsonNode valueNode = body.get("value"); // Assumes data is under "value" key

                if (valueNode != null && valueNode.isArray()) {
                    ArrayNode batchResults = (ArrayNode) valueNode;
                    aggregatedResults.addAll(batchResults);
                    skip += batchResults.size();
                    hasMore = batchResults.size() == top; // If less than `top`, assume no more data
                } else {
                    hasMore = false;
                }
            }
            return Optional.of(aggregatedResults);
        } catch (Exception e) {
            log.error("Error fetching batches for {}: {}", objectType, e.getMessage(), e);
            return Optional.empty();
        }
    }

    public static void writeCsv(String dir, String filename, List<String> headers, List<List<String>> rows) throws IOException {
        Path outputPath = Paths.get(dir);
        if (Files.notExists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        File file = new File(outputPath.toFile(), filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.join(",", headers));
            writer.newLine();

            for (List<String> row : rows) {
                writer.write(String.join(",", row));
                writer.newLine();
            }
        }

        System.out.println("✅ File written: " + file.getAbsolutePath());
    }
}