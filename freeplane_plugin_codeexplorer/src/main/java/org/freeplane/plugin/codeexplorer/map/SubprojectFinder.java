/*
 * Created on 3 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import java.util.stream.Stream;

import com.tngtech.archunit.core.domain.JavaClass;

interface SubprojectFinder {
    SubprojectFinder EMPTY = new SubprojectFinder() {

        @Override
        public int subprojectIndexOf(JavaClass javaClass) {
            return -1;
        }

        @Override
        public Stream<JavaClass> allClasses() {
            return Stream.empty();
        }
    };
    int subprojectIndexOf(JavaClass javaClass);
    Stream<JavaClass> allClasses();
}
