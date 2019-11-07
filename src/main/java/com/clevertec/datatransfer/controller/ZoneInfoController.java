//package com.clevertec.datatransfer.controller;
//
//import com.clevertec.datatransfer.dto.ZoneInfo;
//import com.clevertec.datatransfer.service.GeoZoneService;
//import com.clevertec.datatransfer.service.ParsingService;
//import com.clevertec.datatransfer.utils.Constants;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.text.ParseException;
//import java.util.List;
//
//@RestController
//@RequestMapping(Constants.REQUEST_MAPPING)
//@RequiredArgsConstructor
//@Slf4j
//public class ZoneInfoController {
//
//    private final ParsingService parsingService;
//    private final GeoZoneService geoZoneService;
//
//    @PostMapping(value = Constants.CREATE_GEOZONES_MAPPING)
//    ResponseEntity<?> createGeozones(@RequestParam MultipartFile file) {
//        log.info("parseCsv() called, file : {}", file);
//        try {
//            List<ZoneInfo> infoList = parsingService.parse(file);
//            List<ZoneInfo> result = geoZoneService.createGeoZones(infoList);
//            geoZoneService.getZonesInfo(null);
//            return ResponseEntity.ok(result);
//        } catch (ParseException e) {
//            log.info("ParseException : {}", e.getMessage());
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    @PostMapping(value = Constants.GET_GEOZONES_IDS_MAPPING)
//    ResponseEntity<?> getGeozonesIds() {
//        return ResponseEntity.ok(geoZoneService.getZonesIds());
//    }
//
//    @PostMapping(value = Constants.GET_GEOZONES_INFO_MAPPING)
//    ResponseEntity<?> getGeozonesInfo(@RequestBody List<Long> ids) {
//        return ResponseEntity.ok(geoZoneService.getZonesInfo(ids));
//    }
//
//    @PostMapping(value = Constants.DELETE_GEOZONES_MAPPING)
//    ResponseEntity<?> createGeozones(@RequestBody List<Long> ids) {
//        return ResponseEntity.ok(geoZoneService.deleteZones(ids));
//    }
//
//}