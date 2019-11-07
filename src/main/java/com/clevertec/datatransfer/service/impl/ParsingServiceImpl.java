package com.clevertec.datatransfer.service.impl;

import com.clevertec.datatransfer.dto.ZoneInfo;
import com.clevertec.datatransfer.service.ParsingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsefa.DeserializationException;
import net.sf.jsefa.Deserializer;
import net.sf.jsefa.common.lowlevel.filter.HeaderAndFooterFilter;
import net.sf.jsefa.csv.CsvIOFactory;
import net.sf.jsefa.csv.config.CsvConfiguration;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParsingServiceImpl implements ParsingService {

    private final ObjectMapper objectMapper;

    @Override
    public List<ZoneInfo> parse(InputStream file) throws ParseException {
        List<ZoneInfo> zoneInfos = new ArrayList<>();
        CsvConfiguration configuration = new CsvConfiguration();
        configuration.setLineFilter(new HeaderAndFooterFilter(1, false, true));
        Deserializer deserializer = CsvIOFactory.createFactory(configuration, ZoneInfo.class).createDeserializer();
        deserializer.open(new InputStreamReader(file, Charset.forName("Cp1251")));
        try {
            while (deserializer.hasNext()) {
                zoneInfos.add(deserializer.next());
            }
        } catch (DeserializationException e) {
            throw new ParseException(formatExceptionMessage(e), e.getInputPosition().getLineNumber());
        }
        deserializer.close(true);
        return validateInput(zoneInfos);
    }

    private String formatExceptionMessage(DeserializationException e) {
        return "Error while deserializing, line: " + e.getInputPosition().getLineNumber()
                + " , column : " + e.getInputPosition().getColumnNumber();
    }

    private List<ZoneInfo> validateInput(List<ZoneInfo> input) {
        return input.stream().filter(element -> element != null &&
                element.getId() == null).collect(Collectors.toList());
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

}