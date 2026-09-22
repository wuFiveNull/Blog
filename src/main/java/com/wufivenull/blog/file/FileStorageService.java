package com.wufivenull.blog.file;

import com.wufivenull.blog.user.UserAccount;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png", "image/jpeg", "image/gif", "image/webp"
    );

    private final StoredFileRepository storedFileRepository;
    private final Path root;

    public FileStorageService(StoredFileRepository storedFileRepository, Environment environment) {
        this.storedFileRepository = storedFileRepository;
        this.root = Path.of(environment.getProperty("app.storage-path", "./storage/uploads"))
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException exception) {
            throw new IllegalStateException("无法创建上传目录", exception);
        }
    }

    public StoredFile store(MultipartFile file, UserAccount uploader) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择图片");
        }
        String contentType = file.getContentType() == null
                ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("只允许上传 PNG、JPG、GIF 或 WebP 图片");
        }
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null
                ? "image" : file.getOriginalFilename());
        String extension = extension(originalName, contentType);
        String storedName = UUID.randomUUID() + extension;
        Path target = root.resolve(storedName).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("非法文件路径");
        }
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("图片保存失败", exception);
        }
        StoredFile storedFile = new StoredFile(storedName, originalName, contentType,
                file.getSize(), target.toString(), uploader);
        return storedFileRepository.save(storedFile);
    }

    public Resource load(String storedName) {
        if (storedName == null || storedName.contains("..")
                || !storedName.matches("[a-fA-F0-9-]+\\.(png|jpg|jpeg|gif|webp)")) {
            throw new IllegalArgumentException("非法文件名");
        }
        StoredFile storedFile = storedFileRepository.findByStoredName(storedName)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在"));
        try {
            Resource resource = new UrlResource(Path.of(storedFile.getStoragePath()).toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("文件不可读");
            }
            return resource;
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("文件路径无效", exception);
        }
    }

    private String extension(String filename, String contentType) {
        String extension = StringUtils.getFilenameExtension(filename);
        if (extension != null && Set.of("png", "jpg", "jpeg", "gif", "webp")
                .contains(extension.toLowerCase(Locale.ROOT))) {
            return "." + extension.toLowerCase(Locale.ROOT);
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/gif" -> ".gif";
            default -> ".webp";
        };
    }
}
