package app.web.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum GenreRequest {
    POP("POP");
    private final String code;


    GenreRequest(String code) {
        this.code = code;
    }

    @JsonCreator
    public static GenreRequest getByCode(String code) {
        for (GenreRequest value : GenreRequest.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
