package com.clevertec.datatransfer.service;

import com.clevertec.datatransfer.dto.ZoneInfo;

import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

public interface ParsingService {
    List<ZoneInfo> parse(InputStream file) throws ParseException;
}