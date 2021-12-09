/*
 * Copyright (c) 2021, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.espresso.ffi.nfi;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.espresso.EspressoOptions;
import com.oracle.truffle.espresso.ffi.NativeAccess;
import com.oracle.truffle.espresso.ffi.Pointer;
import com.oracle.truffle.espresso.meta.EspressoError;
import com.oracle.truffle.espresso.runtime.EspressoProperties;
import com.oracle.truffle.espresso.substitutions.Collect;
import org.graalvm.options.OptionValues;
import org.graalvm.polyglot.Engine;

public final class NFISulongNativeAccess extends NFINativeAccess {

    NFISulongNativeAccess(TruffleLanguage.Env env) {
        super(env);
    }

    @Override
    public @Pointer TruffleObject loadLibrary(Path libraryPath) {
        CompilerAsserts.neverPartOfCompilation();
        if (!Files.exists(libraryPath)) {
            return null;
        }
        String nfiSource = String.format("with llvm load(RTLD_LAZY) '%s'", libraryPath);
        return loadLibraryHelper(nfiSource);
    }

    @Override
    public @Pointer TruffleObject loadDefaultLibrary() {
        return null; // not supported
    }

    /**
     * Returns the Java version of the specified Java home directory, as declared in the 'release'
     * file. Compatible only with Java 11+.
     * 
     * @return Java version as declared in the JAVA_VERSION property in the 'release' file, or null
     *         otherwise.
     */
    static String getJavaVersion(Path javaHome) {
        Path releaseFile = javaHome.resolve("release");
        if (!Files.isRegularFile(releaseFile)) {
            return null;
        }
        try {
            for (String line : Files.readAllLines(releaseFile)) {
                if (line.startsWith("JAVA_VERSION=")) {
                    String quotedVersion = line.substring("JAVA_VERSION=".length()).trim();
                    if (quotedVersion.length() > 2) {
                        assert quotedVersion.startsWith("\"") && quotedVersion.endsWith("\"");
                        return quotedVersion.substring(1, quotedVersion.length() - 1);
                    }
                }
            }
        } catch (IOException e) {
            // cannot read file, skip
        }
        return null; // JAVA_VERSION not found
    }

    /**
     * Finds a folder containing libraries with LLVM bitcode compatible with the specified Java
     * version. First checks the 'default' folder, as it matches the Java version of the parent
     * GraalVM. Then checks remaining folders under llvmRoot.
     */
    private Path llvmBootLibraryPath(String javaVersion, Path llvmRoot) {
        // Try $ESPRESSO_HOME/lib/llvm/default first.
        Path llvmDefault = llvmRoot.resolve("default");
        String llvmDefaultVersion = getJavaVersion(llvmDefault);
        getLogger().fine(() -> "Check " + llvmDefault + " with Java version: " + llvmDefaultVersion);
        if (javaVersion.equals(llvmDefaultVersion)) {
            return llvmDefault;
        }

        /*
         * Try directories in $ESPRESSO_HOME/lib/llvm/* for libraries with LLVM-bitcode compatible
         * with the specified Java version.
         */
        for (Path llvmImpl : llvmRoot) {
            if (!llvmDefault.equals(llvmImpl) && Files.isDirectory(llvmImpl)) {
                String llvmImplVersion = getJavaVersion(llvmImpl);
                getLogger().fine(() -> "Checking " + llvmImpl + " with Java version: " + llvmImplVersion);
                if (javaVersion.equals(llvmImplVersion)) {
                    return llvmImpl;
                }
            }
        }

        return null; // not found
    }

    /**
     * Overrides --java.JVMLibraryPath and --java.BootLibraryPath only if those options are not set
     * by the user.
     *
     * Looks for libraries with embedded LLVM bitcode located in languages/java/lib/llvm/* , every
     * folder represents a version
     */
    @Override
    public void updateEspressoProperties(EspressoProperties.Builder builder, OptionValues options) {
        if (options.hasBeenSet(EspressoOptions.BootLibraryPath)) {
            getLogger().warning("--java.BootLibraryPath was set by the user, skip override for " + Provider.ID);
        } else {
            String targetJavaVersion = getJavaVersion(builder.javaHome());
            if (targetJavaVersion == null) {
                getLogger().warning("Cannot determine the Java version for '" + builder.javaHome() + "'. The default --java.BootLibraryPath will be used.");
            } else {
                Path llvmRoot = builder.espressoHome().resolve("lib").resolve("llvm");
                Path llvmBootLibraryPath = llvmBootLibraryPath(targetJavaVersion, llvmRoot);
                if (llvmBootLibraryPath == null) {
                    getLogger().warning("Couldn't find libraries with LLVM bitcode for Java version '" + targetJavaVersion + "'. The default --java.BootLibraryPath will be used.");
                } else {
                    builder.bootLibraryPath(Collections.singletonList(llvmBootLibraryPath));
                }
            }
        }

        /*
         * The location of Espresso's libjvm is updated to point to exactly the same file inside the
         * Espresso home so it can be loaded by Sulong without extra configuration of the Truffle
         * file system.
         */
        if (options.hasBeenSet(EspressoOptions.JVMLibraryPath)) {
            getLogger().warning("--java.JVMLibraryPath was set by the user, skip override for " + Provider.ID);
        } else {
            builder.jvmLibraryPath(Collections.singletonList(builder.espressoHome().resolve("lib")));
        }
    }

    @Collect(NativeAccess.class)
    public static final class Provider implements NativeAccess.Provider {

        final static String ID = "nfi-sulong";

        @Override
        public String id() {
            return ID;
        }

        @Override
        public NativeAccess create(TruffleLanguage.Env env) {
            return new NFISulongNativeAccess(env);
        }
    }
}
