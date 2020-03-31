package com.example.library.repository

import com.example.library.domain.Loan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LoanRepository: JpaRepository<Loan, String>