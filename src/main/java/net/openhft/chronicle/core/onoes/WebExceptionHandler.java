/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.util.URIEncoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

public class WebExceptionHandler implements ExceptionHandler {
    private final Properties properties = new Properties();

    @NotNull
    private final ExceptionHandler fallBack;
    private final String baseURI;

    public WebExceptionHandler(String propertiesFile, @NotNull ExceptionHandler fallBack) {
        assert fallBack != null;
        this.fallBack = fallBack;
        InputStream stream = WebExceptionHandler.class.getResourceAsStream(propertiesFile);
        try {
            if (stream != null)
                properties.load(stream);
        } catch (IOException e) {
            Slf4jExceptionHandler.WARN.on(getClass(), "Unable to load " + propertiesFile, e);
        }
        baseURI = properties.getProperty("baseUri", "http://stackoverflow.com/search?q=%5Bjava%5D");
    }

    @Override
    public void on(@NotNull Class clazz, @Nullable String message, @NotNull Throwable t) {
        while (t.getCause() != null && t.getCause() != t)
            t = t.getCause();
        String uri = properties.getProperty(t.getClass().getName());
        if (uri == null) {
            uri = baseURI;
            String version = System.getProperty("java.version");
            if (version.compareTo("1.5") >= 0) {
                @NotNull String[] parts = version.split("\\.");
                version = parts[1];
            }

            uri += "+" + version + "+" + URIEncoder.encodeURI(t.toString())
                    + "+" + URIEncoder.encodeURI(clazz.getSimpleName());
            if (message != null)
                uri += "+" + URIEncoder.encodeURI(message);
        }
        try {
            if (Jvm.isDebug() && Desktop.isDesktopSupported())
                Desktop.getDesktop().browse(new URI(uri));
            else
                fallBack.on(clazz, message, t);

        } catch (Exception e) {
            fallBack.on(WebExceptionHandler.class, "Failed to open browser", e);
        }
    }
}
