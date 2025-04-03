package app.record.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Genre {
    POP("POP"),
    ROCK("ROCK"),
    JAZZ("JAZZ"),
    BLUES("BLUES"),
    CLASSICAL("CLASSICAL"),
    RAP("RAP"),
    RNB("RNB"),
    DISCO("DISCO"),
    COUNTRY("COUNTRY"),
    ELECTRONIC("ELECTRONIC"),
    HOUSE("HOUSE"),
    TECHNO("TECHNO"),
    METAL("METAL"),
    PUNK("PUNK"),
    ALTERNATIVE("ALTERNATIVE"),
    FOLK("FOLK"),
    LATIN("LATIN"),
    SOUNDTRACK("SOUNDTRACK"),
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
