/*
 * Copyright 2013-2024 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.asciidoclet;

import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AttributesLoaderTest {

    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();
    private final StubReporter reporter = new StubReporter();

    private Path tmpDir;

    @BeforeEach
    void before(@TempDir Path tmpDir) {
        this.tmpDir = tmpDir;
    }

    @Test
    void testNoAttributes() {
        DocletOptions options = new DocletOptions(reporter);
        AttributesLoader loader = new AttributesLoader(asciidoctor, options, reporter);

        Map<String, Object> attrs = loader.load();

        assertThat(attrs).isEmpty();
        reporter.assertNoMoreInteractions();
    }

    @Test
    void testOnlyCommandLineAttributes() {
        DocletOptions options = new DocletOptions(reporter);
        options.collect(AsciidocletOptions.ATTRIBUTE, List.of("foo=bar, foo2=foo-two, not!, override=override@"));
        AttributesLoader loader = new AttributesLoader(asciidoctor, options, reporter);

        Map<String, Object> attrs = loader.load();

        assertThat(attrs)
                .containsEntry("foo", "bar")
                .containsEntry("foo2", "foo-two")
                .containsEntry("override", "override@")
                .containsKey("not!");
        assertThat(attrs)
                .doesNotContainKey("not");
        reporter.assertNoMoreInteractions();
    }

    @Test
    void testOnlyCommandLineAttributesMulti() {
        DocletOptions options = new DocletOptions(reporter);
        options.collect(AsciidocletOptions.ATTRIBUTE, List.of(
                "foo=bar", "foo2=foo two", "not!", "override=override@"));
        AttributesLoader loader = new AttributesLoader(asciidoctor, options, reporter);

        Map<String, Object> attrs = loader.load();

        assertThat(attrs)
                .containsEntry("foo", "bar")
                .containsEntry("foo2", "foo two")
                .containsEntry("override", "override@")
                .containsKey("not!");
        assertThat(attrs)
                .doesNotContainKey("not");
        reporter.assertNoMoreInteractions();
    }

    @Test
    void testOnlyAttributesFile() throws IOException {
        Path attrsFile = createTempFile("attrs.adoc", ATTRS);

        DocletOptions options = new DocletOptions(reporter);
        options.collect(AsciidocletOptions.ATTRIBUTES_FILE, List.of(attrsFile.toAbsolutePath().toString()));
        AttributesLoader loader = new AttributesLoader(asciidoctor, options, reporter);

        Map<String, Object> attrs = loader.load();

        assertThat(attrs)
                .containsEntry("foo", "BAR")
                .containsEntry("foo2", "BAR-TWO")
                .containsEntry("override", "OVERRIDE")
                .containsKey("not");
        reporter.assertNoMoreInteractions();
    }

    @Test
    void testCommandLineAndAttributesFile() throws IOException {
        Path attrsFile = createTempFile("attrs.adoc", ATTRS);

        DocletOptions options = new DocletOptions(reporter);
        options.collect(AsciidocletOptions.ATTRIBUTE, List.of("foo=bar, not!, override=override@"));
        options.collect(AsciidocletOptions.ATTRIBUTES_FILE, List.of(attrsFile.toAbsolutePath().toString()));
        AttributesLoader loader = new AttributesLoader(asciidoctor, options, reporter);

        Map<String, Object> attrs = new HashMap<>(loader.load());

        assertThat(attrs)
                .containsEntry("foo", "bar")
                .containsEntry("foo2", "bar-TWO")
                .containsEntry("override", "OVERRIDE")
                .containsKey("not!");
        assertThat(attrs)
                .doesNotContainKey("not");
        reporter.assertNoMoreInteractions();
    }

    @Test
    void testAttributesFileIncludeFromBaseDir() throws IOException {
        Path attrsFile = createTempFile("attrs.adoc", "include::attrs-include.adoc[]");
        createTempFile("attrs-include.adoc", ATTRS);

        DocletOptions options = new DocletOptions(reporter);
        options.collect(AsciidocletOptions.ATTRIBUTES_FILE, List.of(attrsFile.toAbsolutePath().toString()));
        options.collect(AsciidocletOptions.BASEDIR, List.of(attrsFile.getParent().toAbsolutePath().toString()));
        AttributesLoader loader = new AttributesLoader(asciidoctor, options, reporter);

        Map<String, Object> attrs = loader.load();

        assertThat(attrs)
                .containsEntry("foo", "BAR")
                .containsEntry("foo2", "BAR-TWO")
                .containsEntry("override", "OVERRIDE")
                .containsKey("not");

        reporter.assertNoMoreInteractions();
    }

    @Test
    void testAttributesFileIncludeFromOtherDir() throws IOException {
        Path attrsFile = createTempFile("attrs.adoc", "include::{includedir}/attrs-include.adoc[]");
        createTempFile("foo", "attrs-include.adoc", ATTRS);

        DocletOptions options = new DocletOptions(reporter);
        options.collect(AsciidocletOptions.ATTRIBUTES_FILE, List.of(attrsFile.toAbsolutePath().toString()));
        options.collect(AsciidocletOptions.BASEDIR, List.of(attrsFile.getParent().toAbsolutePath().toString()));
        options.collect(AsciidocletOptions.ATTRIBUTE, List.of("includedir=foo"));
        AttributesLoader loader = new AttributesLoader(asciidoctor, options, reporter);

        Map<String, Object> attrs = loader.load();

        assertThat(attrs)
                .containsEntry("foo", "BAR")
                .containsEntry("foo2", "BAR-TWO")
                .containsEntry("override", "OVERRIDE")
                .containsKey("not");

        reporter.assertNoMoreInteractions();
    }

    private Path createTempFile(String name, String content) throws IOException {
        return writeFile(content, tmpDir.resolve(name));
    }

    private Path createTempFile(String dir, String name, String content) throws IOException {
        Path directory = tmpDir.resolve(dir);
        Files.createDirectories(directory);
        return writeFile(content, directory.resolve(name));
    }

    private Path writeFile(String content, Path file) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        Files.write(file, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        return file;
    }

    static final String ATTRS =
            ":foo: BAR\n" +
                    ":foo2: {foo}-TWO\n" +
                    ":not: FOO\n" +
                    ":override: OVERRIDE\n";
}
