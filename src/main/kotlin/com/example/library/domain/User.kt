package com.example.library.domain

import org.hibernate.annotations.NaturalId
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
data class User(
        @Id
        val id: String = UUID.randomUUID().toString().toUpperCase(),
        val name: String,
        @NaturalId
        val documentId: String,
        @OneToMany(mappedBy = "user")
        val loans: Set<Loan> = mutableSetOf()
)