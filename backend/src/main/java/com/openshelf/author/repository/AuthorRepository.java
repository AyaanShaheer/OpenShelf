package com.openshelf.author.repository;

import com.openshelf.author.domain.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    Optional<Author> findByFullNameIgnoreCase(String fullName);
}
