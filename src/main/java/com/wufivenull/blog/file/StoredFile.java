package com.wufivenull.blog.file;

import com.wufivenull.blog.user.UserAccount;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
public class StoredFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String storedName;

    @Column(nullable = false, length = 255)
    private String originalName;

    @Column(nullable = false, length = 120)
    private String contentType;

    @Column(nullable = false)
    private long fileSize;

    @Column(nullable = false, length = 500)
    private String storagePath;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploader_id", nullable = false)
    private UserAccount uploader;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected StoredFile() {
    }

    public StoredFile(String storedName, String originalName, String contentType,
                      long fileSize, String storagePath, UserAccount uploader) {
        this.storedName = storedName;
        this.originalName = originalName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.storagePath = storagePath;
        this.uploader = uploader;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getStoredName() {
        return storedName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getStoragePath() {
        return storagePath;
    }
}
