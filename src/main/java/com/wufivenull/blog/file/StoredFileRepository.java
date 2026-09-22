package com.wufivenull.blog.file;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {

    Optional<StoredFile> findByStoredName(String storedName);
}
