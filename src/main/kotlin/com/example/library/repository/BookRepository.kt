package com.example.library.repository

import com.example.library.domain.Book
import org.springframework.data.jpa.repository.JpaRepository

interface BookRepository: JpaRepository<Book, String>