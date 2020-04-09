package com.example.library.repository

import com.example.library.domain.Loan
import org.springframework.data.jpa.repository.JpaRepository

interface LoanRepository: JpaRepository<Loan, String>