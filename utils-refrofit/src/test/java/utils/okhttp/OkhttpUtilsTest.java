/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils.okhttp;

import static java.lang.Character.isISOControl;
import static java.lang.Character.isWhitespace;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.security.SecureRandom;
import java.util.function.Predicate;
import lombok.val;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.junit.jupiter.api.Test;

class OkhttpUtilsTest {

    @Test
    void isPlainText_long_plain_text() {
        assertThat(
            OkhttpUtils.isPlainText(generateTextBuffer(100)),
            equalTo(true)
        );

        assertThat(
            OkhttpUtils.isPlainText(generateTextResponseBody(100)),
            equalTo(true)
        );
    }

    @Test
    void isPlainText_short_plain_text() {
        assertThat(
            OkhttpUtils.isPlainText(generateTextBuffer(10)),
            equalTo(true)
        );

        assertThat(
            OkhttpUtils.isPlainText(generateTextResponseBody(10)),
            equalTo(true)
        );
    }

    @Test
    void isPlainText_plain_text_then_binary() {
        assertThat(
            OkhttpUtils.isPlainText(new Buffer()
                .writeUtf8(generateTextString(64))
                .writeUtf8(generateBinaryString(64))
            ),
            equalTo(true)
        );

        assertThat(
            OkhttpUtils.isPlainText(ResponseBody.create(
                null,
                generateTextString(64) + generateBinaryString(64)
            )),
            equalTo(true)
        );
    }

    @Test
    void isPlainText_long_binary() {
        assertThat(
            OkhttpUtils.isPlainText(generateBinaryBuffer(100)),
            equalTo(false)
        );

        assertThat(
            OkhttpUtils.isPlainText(generateBinaryResponseBody(100)),
            equalTo(false)
        );
    }

    @Test
    void isPlainText_short_binary() {
        assertThat(
            OkhttpUtils.isPlainText(generateBinaryBuffer(10)),
            equalTo(false)
        );

        assertThat(
            OkhttpUtils.isPlainText(generateBinaryResponseBody(10)),
            equalTo(false)
        );
    }

    @Test
    void isPlainText_binary_then_plain_text() {
        assertThat(
            OkhttpUtils.isPlainText(new Buffer()
                .writeUtf8(generateBinaryString(64))
                .writeUtf8(generateTextString(64))
            ),
            equalTo(false)
        );

        assertThat(
            OkhttpUtils.isPlainText(ResponseBody.create(
                null,
                generateBinaryString(64) + generateTextString(64)
            )),
            equalTo(false)
        );
    }


    private static String generateTextString(int length) {
        return generateString(length, ch -> !isISOControl(ch) || isWhitespace(ch));
    }

    private static Buffer generateTextBuffer(int length) {
        val string = generateTextString(length);
        return new Buffer().writeUtf8(string);
    }

    private static ResponseBody generateTextResponseBody(int length) {
        return ResponseBody.create(null, generateTextString(length));
    }

    private static String generateBinaryString(int length) {
        return generateString(length, ch -> isISOControl(ch) && !isWhitespace(ch));
    }

    private static Buffer generateBinaryBuffer(int length) {
        val string = generateBinaryString(length);
        return new Buffer().writeUtf8(string);
    }

    private static ResponseBody generateBinaryResponseBody(int length) {
        return ResponseBody.create(null, generateBinaryString(length));
    }

    private static String generateString(int length, Predicate<Character> charPredicate) {
        val random = new SecureRandom();
        val sb = new StringBuilder();
        while (sb.length() != length) {
            val ch = (char) random.nextInt();
            if (charPredicate.test(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

}
