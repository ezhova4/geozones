package com.clevertec.datatransfer.service;

import com.clevertec.datatransfer.dto.ZoneInfo;
import com.clevertec.datatransfer.entity.ZoneParentInfo;
import com.clevertec.datatransfer.exception.ErrorResponseFromWialonException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface GeoZoneService {
    List<ZoneInfo> createGeoZones(List<ZoneInfo> zones) throws IOException, ErrorResponseFromWialonException;
    Map<Long,List<Long>> getZonesIds() throws IOException, ErrorResponseFromWialonException;
    List<ZoneInfo> getZonesInfo(Map<Long,List<Long>> ids) throws IOException, ErrorResponseFromWialonException;
    void deleteZones(List<ZoneParentInfo> ids) throws IOException, ErrorResponseFromWialonException;
    String getGeozoneImageUrl(String id);
    void setSession(String session);
}
