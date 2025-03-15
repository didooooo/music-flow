package app.record.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Genre {
    POP("POP"),
    UNKNOWN("UNKNOWN");
    private final String code;

    Genre(String code) {
        this.code = code;
    }

    @JsonCreator
    public static Genre getByCode(String code){
        for (Genre value : Genre.values()) {
            if(value.code.equals(code)){
                return value;
            }
        }
        return UNKNOWN;
    }
}
