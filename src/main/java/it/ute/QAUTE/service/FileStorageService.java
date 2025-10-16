package it.ute.QAUTE.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;

@Service
public class FileStorageService {
    private static final String UPLOAD_DIR = "src/main/resources/static/images/avatars";

    private static final Set<String> ALLOWED_EXT = Set.of("png","jpg","jpeg","webp");

    private final Path uploadPath;

    public FileStorageService() {
        this.uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
        } catch (Exception ex) {
            throw new RuntimeException("Không thể tạo thư mục lưu trữ: " + this.uploadPath, ex);
        }
    }

    public String storeFile(MultipartFile file, int accountID) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File upload trống.");
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());

        String ext = extractExtension(original);
        if (ext == null || ext.isBlank()) {
            ext = guessExtByContentType(file.getContentType());
        }
        if (ext == null || !ALLOWED_EXT.contains(ext.toLowerCase(Locale.ROOT))) {
            throw new RuntimeException("Định dạng ảnh không hợp lệ. Chỉ chấp nhận: " + ALLOWED_EXT);
        }

        String fileName = accountID + "." + ext.toLowerCase(Locale.ROOT);

        if (fileName.contains("..")) {
            throw new RuntimeException("Tên file không hợp lệ: " + fileName);
        }

        Path target = this.uploadPath.resolve(fileName).normalize();
        if (!target.startsWith(this.uploadPath)) {
            throw new RuntimeException("Đường dẫn lưu không hợp lệ.");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu file " + fileName + ". Vui lòng thử lại!", ex);
        }

        return "/images/avatars/" + fileName;
    }

    private String extractExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) return null;
        return filename.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    private String guessExtByContentType(String ct) {
        if (ct == null) return null;
        ct = ct.toLowerCase(Locale.ROOT);
        if (ct.equals("image/png")) return "png";
        if (ct.equals("image/jpeg")) return "jpg";
        if (ct.equals("image/jpg")) return "jpg";
        if (ct.equals("image/webp")) return "webp";
        return null;
    }
}
