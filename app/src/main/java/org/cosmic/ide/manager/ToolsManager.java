package org.cosmic.ide.manager;

import android.app.Application;
import android.util.Log;

import org.cosmic.ide.App;
import org.cosmic.ide.common.util.FileUtil;
import org.cosmic.ide.common.util.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class ToolsManager {

    private static final String TAG = "ToolsManager";

    private static Application app;

    public static void init(Application application, Runnable onFinish) {
        app = application;

        CompletableFuture.runAsync(
                () -> {
                    deleteCompilerModules();
                    extractAndroidJar();
                    writeKotlinStdLib();
                    writeKotlinCommonStdLib();
                    writeLambdaStubs();
                })
            .whenComplete(
                (__, error) -> {
                    if (error != null) {
                        Log.d(TAG, "Error extracting tools " + error);
                    }

                    if (onFinish != null) {
                        onFinish.run();
                    }
                });
    }

    private static void deleteCompilerModules() {
        final var compilerModules = new File(FileUtil.getDataDir(), "compiler-modules");

        if (compilerModules.exists()) {
            FileUtil.deleteFile(FileUtil.getDataDir() + "compiler-modules");
        }
    }

    private static void extractAndroidJar() {
        final var androidJar = new File(FileUtil.getClasspathDir(), "android.jar");

        if (!androidJar.exists()) {
            ZipUtil.unzipFromAssets(App.context, "android.jar.zip", FileUtil.getClasspathDir());
        }
    }

    private static void writeKotlinStdLib() {
        final var stdLib = new File(FileUtil.getClasspathDir(), "kotlin-stdlib-1.7.20.jar");

        if (!stdLib.exists()) {
            try {
                FileUtil.writeFile(
                        app.getAssets().open("kotlin-stdlib-1.7.20.jar"), stdLib.getAbsolutePath());
            } catch (IOException e) {
                Log.d(TAG, "Unable to extract kotlin stdlib file");
            }
        }
    }

    private static void writeKotlinCommonStdLib() {
        final var commonStdLib =
                new File(FileUtil.getClasspathDir(), "kotlin-stdlib-common-1.7.20.jar");

        if (!commonStdLib.exists()) {
            try {
                FileUtil.writeFile(
                        app.getAssets().open("kotlin-stdlib-common-1.7.20.jar"), commonStdLib.getAbsolutePath());
            } catch (IOException e) {
                Log.d(TAG, "Unable to extract kotlin common stdlib file");
            }
        }
    }

    private static void writeLambdaStubs() {
        final var lambdaStubs = new File(FileUtil.getClasspathDir(), "core-lambda-stubs.jar");

        if (!lambdaStubs.exists()) {
            try {
                FileUtil.writeFile(
                        app.getAssets().open("core-lambda-stubs.jar"), lambdaStubs.getAbsolutePath());
            } catch (IOException e) {
                Log.d(TAG, "Unable to extract core lambda stubs file");
            }
        }
    }
}