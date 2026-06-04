package net.alek.spawnlistgenerator.util;

import net.alek.spawnlistgenerator.core.ErrorHandler;
import net.alek.spawnlistgenerator.core.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstallPathFinder {

    public static String installPath;

    public static void findGModInstallPath() {
        Logger.Log.info("Determining GMod installation path...");

        String steamPath = getSteamInstallPath();

        if (steamPath == null || steamPath.isBlank()) {
            Logger.Log.error("Steam installation path not found.");
            ErrorHandler.LineUnavailableException();
            return;
        }

        if (checkForGMod(Path.of(steamPath))) {
            return;
        }

        Path libraryFoldersFile = Path.of(
                steamPath,
                "steamapps",
                "libraryfolders.vdf"
        );

        if (!Files.isRegularFile(libraryFoldersFile)) {
            Logger.Log.warn("libraryfolders.vdf not found.");
            return;
        }

        try {
            String content = Files.readString(libraryFoldersFile);

            Pattern pathPattern = Pattern.compile("\"path\"\\s*\"([^\"]+)\"");

            Matcher matcher = pathPattern.matcher(content);

            while (matcher.find()) {
                String libraryPath = matcher.group(1);

                if (libraryPath == null || libraryPath.isBlank()) {
                    continue;
                }

                libraryPath = libraryPath.replace("\\\\", "\\");

                if (checkForGMod(Path.of(libraryPath))) {
                    return;
                }
            }

        } catch (IOException e) {
            Logger.Log.error("Failed to read libraryfolders.vdf");
            ErrorHandler.IOException();
            return;
        }

        Logger.Log.warn("Garry's Mod installation path not found.");
    }

    private static boolean checkForGMod(Path steamLibraryRoot) {

        Path[] candidates = {
                steamLibraryRoot.resolve(
                        Path.of("steamapps", "common", "GarrysMod")
                ),
                steamLibraryRoot.resolve(
                        Path.of("steamapps", "common", "Garry's Mod")
                )
        };

        for (Path candidate : candidates) {
            if (Files.isDirectory(candidate)) {
                setInstallPath(candidate);
                return true;
            }
        }

        return false;
    }

    private static void setInstallPath(Path path) {
        installPath = path.toAbsolutePath().toString();

        if (GUIHandler.gmodPathField != null) {
            GUIHandler.gmodPathField.setText(installPath);
        }

        Logger.Log.info("Found Garry's Mod at: " + installPath);
    }

    private static String getSteamInstallPath() {

        String[] regKeys = {
                "HKCU\\Software\\Valve\\Steam",
                "HKLM\\SOFTWARE\\Valve\\Steam",
                "HKLM\\SOFTWARE\\WOW6432Node\\Valve\\Steam"
        };

        for (String regKey : regKeys) {
            try {
                ProcessBuilder builder = new ProcessBuilder(
                        "reg",
                        "query",
                        regKey,
                        "/v",
                        "InstallPath"
                );

                builder.redirectErrorStream(true);

                Process process = builder.start();

                try (BufferedReader reader =
                             new BufferedReader(
                                     new InputStreamReader(process.getInputStream()))) {

                    String line;

                    while ((line = reader.readLine()) != null) {

                        if (!line.contains("InstallPath")) {
                            continue;
                        }

                        String[] parts = line.trim().split("\\s{4,}");

                        if (parts.length >= 3) {
                            String path = parts[2].trim();

                            if (!path.isBlank()) {
                                process.waitFor();
                                return path;
                            }
                        }
                    }
                }

                process.waitFor();

            } catch (IOException e) {
                ErrorHandler.IOException();

            } catch (InterruptedException e) {
                ErrorHandler.InterruptedException();
                Thread.currentThread().interrupt();
                return null;
            }
        }

        return null;
    }
}