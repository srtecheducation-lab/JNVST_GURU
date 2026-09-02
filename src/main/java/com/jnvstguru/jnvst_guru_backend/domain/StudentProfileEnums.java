package com.jnvstguru.jnvst_guru_backend.domain;

public final class StudentProfileEnums {
    private StudentProfileEnums() {
    }

    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }

    public enum Category {
        GENERAL,
        OBC,
        SC,
        ST
    }

    public enum ResidentialArea {
        RURAL,
        URBAN
    }
}
