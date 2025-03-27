package app.web.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum GenreRequest {
    POP("POP"),
    ROCK("ROCK"),
    JAZZ("JAZZ"),
    BLUES("BLUES"),
    CLASSICAL("CLASSICAL"),
    HIP_HOP("HIP_HOP"),
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
    SOUNDTRACK("SOUNDTRACK"),;
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
