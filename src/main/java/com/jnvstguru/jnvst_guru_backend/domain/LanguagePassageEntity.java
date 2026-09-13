package com.jnvstguru.jnvst_guru_backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "language_passages", schema = "application",
       uniqueConstraints = @UniqueConstraint(columnNames = {"language_code", "batch_no", "passage_number"}))
public class LanguagePassageEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;
    @Column(name = "batch_no", nullable = false, length = 60)
    private String batchNo;
    @Column(name = "passage_number", nullable = false)
    private Integer passageNumber;
    @Column(name = "passage_text", nullable = false, columnDefinition = "TEXT")
    private String passageText;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist void prePersist() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        createdAt = now;
        updatedAt = now;
    }
    @PreUpdate void preUpdate() { updatedAt = OffsetDateTime.now(ZoneOffset.UTC); }
    public Long getId() { return id; }
    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String value) { languageCode = value; }
    public void setLanguageCode(LanguageCode value) { languageCode = value == null ? null : value.code(); }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String value) { batchNo = value; }
    public Integer getPassageNumber() { return passageNumber; }
    public void setPassageNumber(Integer value) { passageNumber = value; }
    public String getPassageText() { return passageText; }
    public void setPassageText(String value) { passageText = value; }
}
