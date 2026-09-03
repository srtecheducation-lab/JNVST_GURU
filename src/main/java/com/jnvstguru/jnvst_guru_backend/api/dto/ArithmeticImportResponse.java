package com.jnvstguru.jnvst_guru_backend.api.dto;

import java.util.List;

public record ArithmeticImportResponse(int importedRows, List<Long> questionIds) {
}
