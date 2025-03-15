package app.web.dto.enums;

import app.record.model.Format;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum FormatRequest {
    CD("CD"),
    VINYL("VINYL"),
    DVD("DVD");
    private final String code;


    FormatRequest(String code) {
        this.code = code;
    }

    @JsonCreator
    public static FormatRequest getByCode(String code) {
        for (FormatRequest value : FormatRequest.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
