package com.waltsoft.tx_flow.entity.basic;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
public class BasicEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    protected UUID id;

    @Column(name = "CREATED_AT",
            insertable = false,
            updatable = false)
    protected LocalDateTime createdAt;

    @Column(name = "UPDATED_AT",
            insertable = false,
            updatable = false)
    protected LocalDateTime updatedAt;

    public BasicEntity() {
    }

    public BasicEntity(BasicEntity other) {
        id = other.id;
        createdAt = other.createdAt;
        updatedAt = other.updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this==o) {
            return true;
        }
        if (!(o instanceof BasicEntity that)) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(createdAt, that.createdAt) && Objects.equals(updatedAt,
                that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt, updatedAt);
    }
}