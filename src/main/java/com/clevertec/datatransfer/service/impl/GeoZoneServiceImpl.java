package com.clevertec.datatransfer.service.impl;

import com.clevertec.datatransfer.dto.ZoneInfo;
import com.clevertec.datatransfer.entity.Error;
import com.clevertec.datatransfer.entity.ZoneParentInfo;
import com.clevertec.datatransfer.exception.ErrorResponseFromWialonException;
import com.clevertec.datatransfer.service.GeoZoneService;
import com.clevertec.datatransfer.utils.SSLUtil;
import com.clevertec.datatransfer.utils.WialonConstants;
import com.clevertec.datatransfer.utils.WialonConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class GeoZoneServiceImpl implements GeoZoneService {

    private final ObjectMapper objectMapper;
    private HttpHeaders wialonHeaders;
    private final RestTemplate restTemplate = new RestTemplate();
    private Long itemId;
    private String session;

    @PostConstruct
    private void init() {
        try {
            SSLUtil.turnOffSslChecking();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        wialonHeaders = new HttpHeaders();
        wialonHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    }

    private MultiValueMap<String, String> createParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(WialonConstants.SID_KEY, session);
        params.add(WialonConstants.SVC_KEY, WialonConstants.SVC_VALUE_GET_GEOZONES_IDS);
        params.add(WialonConstants.PARAMS_KEY, WialonConstants.PARAMS_VALUE_GET_ALL_GEOZONES);
        return params;
    }


    @Override
    public List<ZoneInfo> createGeoZones(List<ZoneInfo> zones) throws IOException, ErrorResponseFromWialonException {
        log.info("createGeoZones() called");
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Set<String> parentNames = new HashSet<>();
        for (ZoneInfo zone : zones) {
            parentNames.add(zone.getParent());
        }
        Map<String, String> nameToIdMapping = convertParentNamesToIds(params, parentNames);
        for (ZoneInfo zone : zones) {
            zone.setParent(nameToIdMapping.get(zone.getParent()));
        }
        params.clear();
        params.add(WialonConstants.SID_KEY, session);
        params.add(WialonConstants.SVC_KEY, WialonConstants.SVC_VALUE_CREATE_MULTIPLE_GEOZONES);
        params.add(WialonConstants.PARAMS_KEY, WialonConverter.createCreateGeozonesParams(zones));
        processRequestToWialon(params);
        return getZonesInfo(getZonesIds());
    }

    @Override
    public Map<Long, List<Long>> getZonesIds() throws IOException, ErrorResponseFromWialonException {
        log.info("getGeoZonesIds() called");
        MultiValueMap<String, String> params = createParams();
        String response = processRequestToWialon(params);
        Map<Long, List<Long>> ids = WialonConverter.extractIdsFromGetIdsResponse(response);
        log.info("getGeoZonesIds() ids: {}", ids);
        return ids;
    }


    @Override
    public List<ZoneInfo> getZonesInfo(Map<Long, List<Long>> ids) throws IOException, ErrorResponseFromWialonException {
        log.info("getZonesInfo() called");
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(WialonConstants.SID_KEY, session);
        params.add(WialonConstants.SVC_KEY, WialonConstants.SVC_VALUE_GET_GEOZONE_INFO);
        params.set(WialonConstants.PARAMS_KEY, WialonConverter.createGetGeozonesInfoParams(ids));
        String response = processRequestToWialon(params);
        List<ZoneInfo> result = new ArrayList<>();
        for (String info : WialonConverter.extractCreatedZonesResponse(response)) {
            result.add(WialonConverter.wialonResponsedZoneInfoToZoneInfo(info));
        }
        params = createParams();
        response = processRequestToWialon(params);
        Map<String, String> parentIdToName = WialonConverter.extractParentIdToName(response, ids.keySet());
        for (ZoneInfo zone : result) {
            zone.setParent(parentIdToName.get(zone.getParent()));
        }
        return result;
    }

    private String processRequestToWialon(MultiValueMap<String, String> params) throws ErrorResponseFromWialonException {
        return sendRequestToWialon(params);
    }


    @Override
    public void deleteZones(List<ZoneParentInfo> zones) throws IOException, ErrorResponseFromWialonException {
        log.info("deleteZones() called");
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Set<String> parentNames = new HashSet<>();
        for (ZoneParentInfo zone : zones) {
            parentNames.add(zone.getParent());
        }
        Map<String, String> nameToIdMapping = convertParentNamesToIds(params, parentNames);
        String response;
        for (ZoneParentInfo zone : zones) {
            zone.setParent(nameToIdMapping.get(zone.getParent()));
        }
        params.clear();
        params.add(WialonConstants.SID_KEY, session);
        params.add(WialonConstants.SVC_KEY, WialonConstants.SVC_VALUE_DELETE_GEOZONES);
        params.set(WialonConstants.PARAMS_KEY, WialonConverter.createDeleteGeozonesParams(zones));
        response = processRequestToWialon(params);
        log.info("deleteZones() response : {}", response);
    }

    private Map<String, String> convertParentNamesToIds(MultiValueMap<String, String> params, Set<String> parentNames) throws IOException, ErrorResponseFromWialonException {
        params.add(WialonConstants.SID_KEY, session);
        params.add(WialonConstants.SVC_KEY, WialonConstants.SVC_VALUE_GET_GEOZONES_IDS_BY_NAME);
        params.add(WialonConstants.PARAMS_KEY, WialonConverter.createGetParentsIdsByNames(parentNames));
        String response = processRequestToWialon(params);
        return WialonConverter.extractNameToIdMapping(response);
    }

    @Override
    public String getGeozoneImageUrl(String id) {
        String parentId = StringUtils.substringBefore(id, "_");
        String zoneId = StringUtils.substringAfterLast(id, "_");
        String response = WialonConstants.IMAGE_HOST + parentId + "/" + zoneId + "/32";
        log.info("getGeozoneImageUrl: " + response);
        return response;
    }

    @Override
    public void setSession(String session) {
        this.session = session;
    }

    private String sendRequestToWialon(MultiValueMap<String, String> requestParams) throws ErrorResponseFromWialonException {
        String response;
        log.info(WialonConstants.LOG_REQUEST_TO_WIALON, WialonConstants.WIALON_URL, requestParams);
        HttpEntity<Object> request = new HttpEntity<>(requestParams, wialonHeaders);
        response = restTemplate.postForObject(WialonConstants.WIALON_URL, request, String.class);
        checkResponseForError(response);
        return response;
    }

    private void checkResponseForError(String response) throws ErrorResponseFromWialonException {
        try {
            Error error = objectMapper.readValue(response, Error.class);
            if (error.getError() != null) {
                log.error(WialonConstants.LOG_ERROR_RESPONSE_FROM_WIALON, error);
                throw new ErrorResponseFromWialonException(error.getError().equals("1") ? WialonConstants.EXPIRED_WIALON_SESSION : String.format(WialonConstants.ERROR_RESPONSE_FROM_WIALON, error), error);
            }
        } catch (IOException ignored) {
        }
    }

    private String toJson(Object o) {
        String json = null;
        try {
            json = objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.warn("Error while parsing object to json");
        }
        return json;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
