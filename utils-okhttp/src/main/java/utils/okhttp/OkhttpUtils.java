package utils.okhttp;

import static java.lang.Character.isISOControl;
import static java.lang.Character.isWhitespace;
import static java.lang.Math.min;

import java.io.EOFException;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.ResponseBody;
import okio.Buffer;

public interface OkhttpUtils {

    @SuppressWarnings("java:S109")
    static boolean isPlainText(Buffer buffer) {
        try {
            val prefix = new Buffer();
            long byteCount = min(buffer.size(), 64);
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (isISOControl(codePoint) && !isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence
        }
    }

    @SneakyThrows
    static boolean isPlainText(ResponseBody responseBody) {
        val source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body
        val buffer = source.getBuffer();
        return isPlainText(buffer);
    }

}
