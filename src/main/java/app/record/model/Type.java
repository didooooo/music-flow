package app.record.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Type {
    SINGLE("SINGLE"),
    ALBUM("ALBUM"),
    UNKNOWN("UNKNOWN");
    private final String code;

    Type(String code) {
        this.code = code;
    }


    @JsonCreator
    public static Type getByCode(String code){
        for (Type value : Type.values()) {
            if(value.code.equals(code)){
                return value;
            }
        }
        return UNKNOWN;
    }
}
