package com.clevertec.datatransfer.dto;

import lombok.Data;
import net.sf.jsefa.csv.annotation.CsvDataType;
import net.sf.jsefa.csv.annotation.CsvField;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Component
@Data
@CsvDataType
public class ZoneInfo {
    @CsvField(pos = 1)
    private String id;
    @CsvField(pos = 2)
    private String title;
    @CsvField(pos = 3)
    private String description;
    @CsvField(pos = 4)
    private String  parent;
    @CsvField(pos = 5)
    private String radius;
    @CsvField(pos = 6)
    private String latitude;
    @CsvField(pos = 7)
    private String longitude;
    @CsvField(pos = 8)
    private String  zoneColor;
    @CsvField(pos = 9)
    private String signatureColor;
    @CsvField(pos = 10)
    private Integer fontHeight;
    @CsvField(pos = 11)
    @Min(value = 1)
    @Max(value = 8)
    private Integer scale;
}
