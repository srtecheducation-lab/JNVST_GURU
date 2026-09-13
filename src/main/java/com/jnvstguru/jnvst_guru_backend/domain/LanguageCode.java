package com.jnvstguru.jnvst_guru_backend.domain;

public enum LanguageCode {
    ENGLISH("en"),
    HINDI("hi"),
    BENGALI("bn");

    private final String code;

    LanguageCode(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static LanguageCode fromCode(String code) {
        for (LanguageCode language : values()) {
            if (language.code.equalsIgnoreCase(code)) {
                return language;
            }
        }
        throw new IllegalArgumentException("Unsupported language code: " + code);
    }
}
