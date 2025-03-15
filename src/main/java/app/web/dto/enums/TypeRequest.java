package app.web.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TypeRequest {
    SINGLE("SINGLE"),
    ALBUM("ALBUM");
    private final String code;

    TypeRequest(String code) {
        this.code = code;
    }

    @JsonCreator
    public static TypeRequest getByCode(String code) {
        for (TypeRequest value : TypeRequest.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
