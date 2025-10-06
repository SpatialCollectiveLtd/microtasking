package com.spatialcollective.microtasktoolapi.model.audit

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.util.*
import javax.persistence.*


@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
@JsonIgnoreProperties(value = ["createdAt", "updatedAt"], allowGetters = true)
abstract class DateAudit : Serializable {
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Date = Date()

    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Date = Date()
}