package com.anjia.unidbgserver.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.DigestUtils;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public class TempFileUtils {

    private static final Map<String, File> TEMP_FILES = new HashMap<>();

    /**
     * 获取临时文件。如果临时文件不存在，从classpath复制。
     *
     * @param classpathFile classpath下的资源路径
     * @return 临时文件对象
     */
    public static File getTempFile(String classpathFile) {
        try {
            String md5 = DigestUtils.md5DigestAsHex(classpathFile.getBytes());
            if (TEMP_FILES.containsKey(md5)) {
                return TEMP_FILES.get(md5);
            }

            ClassPathResource resource = new ClassPathResource(classpathFile);
            if (!resource.exists()) {
                log.error("资源文件不存在: {}", classpathFile);
                return null;
            }

            // 获取文件扩展名
            String extension = "";
            int dotIndex = classpathFile.lastIndexOf(".");
            if (dotIndex > 0) {
                extension = classpathFile.substring(dotIndex);
            }

            // 创建临时文件
            File tempFile = File.createTempFile("unidbg_", extension);
            tempFile.deleteOnExit();

            // 复制资源到临时文件
            try (InputStream is = resource.getInputStream();
                 FileOutputStream fos = new FileOutputStream(tempFile)) {
                StreamUtils.copy(is, fos);
            }

            TEMP_FILES.put(md5, tempFile);
            log.debug("临时文件创建成功: {} -> {}", classpathFile, tempFile.getAbsolutePath());
            return tempFile;
        } catch (IOException e) {
            log.error("创建临时文件失败: " + classpathFile, e);
            return null;
        }
    }

    /**
     * 清理所有临时文件
     */
    public static void cleanup() {
        for (File file : TEMP_FILES.values()) {
            try {
                if (file.exists() && !file.delete()) {
                    file.deleteOnExit();
                }
            } catch (Exception e) {
                log.warn("删除临时文件失败: {}", file.getAbsolutePath(), e);
            }
        }
        TEMP_FILES.clear();
    }
}
