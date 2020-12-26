package utils.test.resource;

import static java.io.File.createTempFile;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.copy;
import static utils.test.whocalled.WhoCalled.getCallerClass;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import lombok.val;
import org.intellij.lang.annotations.Language;

public abstract class Resources {

    public static URL getResourceUrl(@Language("file-reference") String resourceName) {
        val loaderClass = getCallerClass(1);
        return getResourceUrl(loaderClass, resourceName);
    }

    public static URL getResourceUrl(Class<?> loaderClass, @Language("file-reference") String resourceName) {
        URL resourceUrl = loaderClass.getResource(resourceName);
        if (resourceUrl == null) {
            resourceUrl = loaderClass.getResource('/' + resourceName);
        }
        if (resourceUrl == null) {
            throw new IllegalArgumentException(loaderClass + ": resource can't be found: " + resourceName);
        }
        return resourceUrl;
    }


    public static Path getResourcePath(@Language("file-reference") String resourceName) {
        val loaderClass = getCallerClass(1);
        return getResourcePath(loaderClass, resourceName);
    }

    @SneakyThrows
    public static Path getResourcePath(Class<?> loaderClass, @Language("file-reference") String resourceName) {
        val url = getResourceUrl(loaderClass, resourceName);
        if ("file".equals(url.getProtocol())) {
            return Paths.get(url.toURI());
        }

        final String fileNamePrefix;
        final String fileNameSuffix;
        {
            String fileName = resourceName;
            int delimPos;
            if ((delimPos = fileName.lastIndexOf('/')) >= 0) {
                fileName = fileName.substring(delimPos + 1);
            }
            if ((delimPos = fileName.lastIndexOf('\\')) >= 0) {
                fileName = fileName.substring(delimPos + 1);
            }
            delimPos = fileName.lastIndexOf('.');
            if (delimPos >= 0) {
                fileNamePrefix = fileName.substring(0, delimPos);
                fileNameSuffix = '.' + fileName.substring(delimPos + 1);
            } else {
                fileNamePrefix = fileName;
                fileNameSuffix = "";
            }
        }
        val tempFile = createTempFile(fileNamePrefix, fileNameSuffix);
        tempFile.deleteOnExit();

        val tempFilePath = tempFile.toPath();
        try (val inputStream = url.openStream()) {
            copy(inputStream, tempFilePath);
        }

        return tempFilePath;
    }


    public static byte[] readBinaryResource(@Language("file-reference") String resourceName) {
        val loaderClass = getCallerClass(1);
        return readBinaryResource(loaderClass, resourceName);
    }

    @SneakyThrows
    public static byte[] readBinaryResource(Class<?> loaderClass, @Language("file-reference") String resourceName) {
        val resourceUrl = getResourceUrl(loaderClass, resourceName);
        try (val in = resourceUrl.openStream()) {
            try (val out = new ByteArrayOutputStream()) {
                val buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, read);
                }
                return out.toByteArray();
            }
        }
    }


    public static String readTextResource(@Language("file-reference") String resourceName) {
        val loaderClass = getCallerClass(1);
        return readTextResource(loaderClass, resourceName);
    }

    public static String readTextResource(Class<?> loaderClass, @Language("file-reference") String resourceName) {
        val bytesContent = readBinaryResource(loaderClass, resourceName);
        return new String(bytesContent, UTF_8);
    }


    private Resources() {
    }

}
