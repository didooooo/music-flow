package app.record.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.text.Normalizer;

public enum Format {
    CD("CD"),
    VINYL("VINYL"),
    DVD("DVD"),
    UNKNOWN("UNKNOWN");
    private final String code;

    Format(String code) {
        this.code = code;
    }

    @JsonCreator
    public static Format getByCode(String code) {
        for (Format value : Format.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return UNKNOWN;
    }
}
