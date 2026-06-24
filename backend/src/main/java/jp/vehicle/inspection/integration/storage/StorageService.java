package jp.vehicle.inspection.integration.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Optional;

public interface StorageService {
    String upload(String key, MultipartFile file) throws Exception;
    Optional<InputStream> download(String key) throws Exception;
    void delete(String key) throws Exception;
    String getProvider();
}
