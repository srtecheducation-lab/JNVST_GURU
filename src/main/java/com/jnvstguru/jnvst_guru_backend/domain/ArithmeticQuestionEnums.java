package com.jnvstguru.jnvst_guru_backend.domain;

public final class ArithmeticQuestionEnums {
    private ArithmeticQuestionEnums() {
    }

    public enum QuestionType {
        NUMBER_SYSTEM,
        FRACTION,
        DECIMAL,
        ARITHMETIC_OPERATION,
        BODMAS,
        FACTORS_MULTIPLES,
        RATIO,
        PERCENTAGE,
        PROFIT_LOSS,
        GEOMETRY,
        MENSURATION,
        DATA_INTERPRETATION,
        OTHER
    }

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    public enum Language {
        ENGLISH,
        BENGALI
    }

    public enum Status {
        ACTIVE,
        INACTIVE,
        ARCHIVED
    }
}
