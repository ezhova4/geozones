package com.clevertec.datatransfer.utils;

import com.clevertec.datatransfer.dto.ZoneInfo;
import com.clevertec.datatransfer.entity.ZoneParentInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class WialonConverter {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static List<String> extractCreatedZonesResponse(String response) throws IOException {
        JsonNode responseArray = objectMapper.readTree(response);
        List<String> strings = new ArrayList<>();
        for (JsonNode items : responseArray) {
            if (items.isArray() && items.size() != 0) {
                for (JsonNode item : items) {
                    strings.add(item.toString());
                }
            }
        }
        return strings;
    }

    public static ZoneInfo wialonResponsedZoneInfoToZoneInfo(String response) throws IOException {
        JsonNode node = objectMapper.readTree(response);
        ZoneInfo zoneInfo = new ZoneInfo();
        zoneInfo.setId(node.get("rid").asText() + "_" + node.get("id").asText());
        zoneInfo.setTitle(node.get("n").asText());
        zoneInfo.setDescription(node.get("d").asText());
        zoneInfo.setParent(node.get("rid").asText());
        zoneInfo.setSignatureColor(decToHex(node.get("tc").asText()));
        zoneInfo.setScale(node.get("max").asInt());
        zoneInfo.setFontHeight(node.get("ts").asInt());
        zoneInfo.setZoneColor(decToHex(node.get("c").asText()));

        node = node.get("p").get(0);

        zoneInfo.setRadius(node.get("r").asText());
        zoneInfo.setLatitude(node.get("y").asText());
        zoneInfo.setLongitude(node.get("x").asText());
        return zoneInfo;
    }

    public static Map<Long, List<Long>> extractIdsFromGetIdsResponse(String response) throws IOException {
        Map<Long, List<Long>> ids = new HashMap<>();
        JsonNode items = objectMapper.readTree(response).get("items");
        for (JsonNode item : items) {
            Long parentId = item.get("id").asLong();
            List<Long> childIds = new ArrayList<>();
            if (item.get("zl") != null) {
                for (Iterator<JsonNode> it = item.get("zl").elements(); it.hasNext(); ) {
                    JsonNode zoneInfo = it.next();
                    childIds.add(zoneInfo.get("id").asLong());
                }
                ids.put(parentId, childIds);
            }
        }
        return ids;

    }

    public static Long extractItemId(String response) throws IOException {
        return objectMapper.readTree(response).get("items").get(0).get("id").asLong();
    }

    public static String createDeleteGeozonesParams(List<ZoneParentInfo> zones) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (ZoneParentInfo zone : zones) {
            Map<String, String> map = new HashMap<>();
            map.put("id", zone.getZoneId().substring(zone.getZoneId().lastIndexOf("_") + 1));
            map.put("itemId", zone.getParent());

            StringSubstitutor substitutor = new StringSubstitutor(map);
            String params = substitutor.replace(WialonConstants.DELETE_ITEM_PARAMS_TEMPLATE);

            map.clear();
            map.put(WialonConstants.SVC_KEY, WialonConstants.SVC_VALUE_DELETE_ZONES);
            map.put(WialonConstants.PARAMS_KEY, params);
            substitutor = new StringSubstitutor(map);
            String item = substitutor.replace(WialonConstants.DELETE_ITEM_TEMPLATE);
            builder.append(item);
            builder.append(",");
        }
        builder.replace(builder.length() - 1, builder.length(), "]");
        return builder.toString();
    }


    public static String createCreateGeozonesParams(List<ZoneInfo> zones) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (ZoneInfo zone : zones) {
            Map<String, String> map = objectMapper.readValue(toJson(zone), Map.class);
            map.put("zoneColor", hexToDec(zone.getZoneColor()));
            map.put("signatureColor", hexToDec(zone.getSignatureColor()));
            map.put("itemId", zone.getParent());
            StringSubstitutor substitutor = new StringSubstitutor(map);
            String params = substitutor.replace(WialonConstants.CREATE_ITEM_PARAMS_TEMPLATE);

            map.clear();
            map.put(WialonConstants.SVC_KEY, WialonConstants.SVC_VALUE_CREATE_ZONE);
            map.put(WialonConstants.PARAMS_KEY, params);
            substitutor = new StringSubstitutor(map);
            String item = substitutor.replace(WialonConstants.CREATE_ITEM_TEMPLATE);
            builder.append(item);
            builder.append(",");
        }
        builder.replace(builder.length() - 1, builder.length(), "]");
        return builder.toString();
    }

    public static String createGetGeozonesInfoParams(Map<Long, List<Long>> ids) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Map.Entry<Long, List<Long>> entry : ids.entrySet()) {
            Map<String, String> map = new HashMap<>();
            map.put("itemId", String.valueOf(entry.getKey()));
            map.put("ids", toJson(entry.getValue()));

            StringSubstitutor substitutor = new StringSubstitutor(map);
            String item = substitutor.replace(WialonConstants.GET_GEOZONE_INFO_ITEM_TEMPLATE);

            builder.append(item);
            builder.append(",");
        }
        builder.replace(builder.length() - 1, builder.length(), "]");
        return builder.toString();
    }

    public static String createGetParentsIdsByNames(Set<String> parentNames) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (String parentName : parentNames) {
            Map<String, String> map = new HashMap();
            map.put("parentName", parentName);
            StringSubstitutor substitutor = new StringSubstitutor(map);
            String params = substitutor.replace(WialonConstants.GET_PARENT_ID_BY_NAME_ITEM_PARAMS_TEMPLATE);

            map.clear();
            map.put(WialonConstants.PARAMS_KEY, params);
            substitutor = new StringSubstitutor(map);
            String item = substitutor.replace(WialonConstants.GET_PARENT_ID_BY_NAME_ITEM_TEMPLATE);
            builder.append(item);
            builder.append(",");
        }
        builder.replace(builder.length() - 1, builder.length(), "]");
        return builder.toString();
    }

    public static Map<String, String> extractNameToIdMapping(String response) throws IOException {
        Map<String, String> mapping = new HashMap<>();
        JsonNode root = objectMapper.readTree(response);
        for (Iterator<JsonNode> it = root.elements(); it.hasNext(); ) {
            JsonNode item = it.next().get("items").get(0);
            if (item != null) {

                mapping.put(item.get("nm").asText(), item.get("id").asText());
            }
        }
        return mapping;

    }

    private static String toJson(Object o) {
        String json = null;
        try {
            json = objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.warn("Error while parsing object to json");
        }
        return json;
    }

    private static String hexToDec(String hex) {
        String dec;
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        dec = String.valueOf(Long.parseLong(hex, 16));
        return dec;
    }

    private static String decToHex(String dec) {
        String hex;
        hex = "#" + Long.toHexString(Long.parseLong(dec));
        return hex;
    }


    public static Map<String, String> extractParentIdToName(String response, Set<Long> ids) throws IOException {
        Map<String, String> parentIdToName = new HashMap<>();
        JsonNode items = objectMapper.readTree(response).get("items");
        for (JsonNode item : items) {
            parentIdToName.put(item.get("id").asText(), item.get("nm").asText());
        }
        return parentIdToName;
    }
}
