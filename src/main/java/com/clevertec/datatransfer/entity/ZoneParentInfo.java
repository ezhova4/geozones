package com.clevertec.datatransfer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ZoneParentInfo {
    private String zoneId;
    private String parent;
}
