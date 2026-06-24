package jp.vehicle.inspection.integration.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path basePath;

    public LocalStorageService(@Value("${app.storage.local-path:./storage}") String localPath) throws Exception {
        this.basePath = Path.of(localPath).toAbsolutePath();
        Files.createDirectories(basePath);
    }

    @Override
    public String upload(String key, MultipartFile file) throws Exception {
        Path target = basePath.resolve(key);
        Files.createDirectories(target.getParent());
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        log.info("Stored locally: {}", target);
        return key;
    }

    @Override
    public Optional<InputStream> download(String key) throws Exception {
        Path target = basePath.resolve(key);
        if (!Files.exists(target)) return Optional.empty();
        return Optional.of(Files.newInputStream(target));
    }

    @Override
    public void delete(String key) throws Exception {
        Files.deleteIfExists(basePath.resolve(key));
    }

    @Override
    public String getProvider() {
        return "local";
    }
}
