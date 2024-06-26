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

import com.sun.tools.javac.parser.LazyDocCommentTable;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.util.Context;
import org.junit.jupiter.api.Test;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

class LazyDocCommentTableProcessorTest {

    @Test
    void testProcessComments() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Just make sure the processComments method returns normally for now.
        LazyDocCommentTableProcessor.processComments(createLazyDocCommentTable(), commentMapper());
    }

    private LazyDocCommentTable createLazyDocCommentTable() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<LazyDocCommentTable> constructor = LazyDocCommentTable.class.getDeclaredConstructor(ParserFactory.class);
        constructor.setAccessible(true);

        return constructor.newInstance(createParserFactory());
    }

    private Function<Comment, Comment> commentMapper() {
        return Function.identity();
    }

    private ParserFactory createParserFactory() {
        return ParserFactory.instance(createContext());
    }

    private static Context createContext() {
        Context context = new Context();
        context.put(JavaFileManager.class, createJavaFileManager());
        return context;
    }

    private static JavaFileManager createJavaFileManager() {
        return new JavaFileManager() {
            @Override
            public ClassLoader getClassLoader(Location location) {
                return null;
            }

            @Override
            public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
                return null;
            }

            @Override
            public String inferBinaryName(Location location, JavaFileObject file) {
                return null;
            }

            @Override
            public boolean isSameFile(FileObject a, FileObject b) {
                return false;
            }

            @Override
            public boolean handleOption(String current, Iterator<String> remaining) {
                return false;
            }

            @Override
            public boolean hasLocation(Location location) {
                return false;
            }

            @Override
            public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
                return null;
            }

            @Override
            public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
                return null;
            }

            @Override
            public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
                return null;
            }

            @Override
            public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
                return null;
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() {

            }

            @Override
            public int isSupportedOption(String option) {
                return 0;
            }
        };
    }
}
