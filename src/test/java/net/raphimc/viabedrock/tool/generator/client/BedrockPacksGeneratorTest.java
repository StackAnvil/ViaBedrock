/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.viabedrock.tool.generator.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

final class BedrockPacksGeneratorTest {

    @TempDir
    Path directory;

    @Test
    void includesExtractedDefinitionsAtThePackRoot() throws Exception {
        final Path input = Files.createDirectory(this.directory.resolve("input"));
        final byte[] payload = {1, 2, 3};
        for (String folder : new String[]{"biomes", "fogs", "render_controllers"}) {
            write(input.resolve("__brarchive/" + folder + "/nested/definition.json"), payload);
        }
        final Path output = this.directory.resolve("output.mcpack");

        BedrockPacksGenerator.writeResourcePack(input.toFile(), output);

        try (ZipFile pack = new ZipFile(output.toFile())) {
            for (String folder : new String[]{"biomes", "fogs", "render_controllers"}) {
                assertArrayEquals(payload, pack.getInputStream(pack.getEntry(folder + "/nested/definition.json")).readAllBytes());
                assertNull(pack.getEntry("__brarchive/" + folder + "/nested/definition.json"));
            }
        }
    }

    @Test
    void mergesBothLayoutsAndPreservesDirectFilesOnConflicts() throws Exception {
        final Path input = Files.createDirectory(this.directory.resolve("input"));
        final byte[] direct = {1, 2, 3};
        final byte[] extracted = {4, 5, 6};
        write(input.resolve("biomes/direct.json"), direct);
        write(input.resolve("__brarchive/biomes/extracted.json"), extracted);
        write(input.resolve("__brarchive/biomes/direct.json"), extracted);
        final Path output = this.directory.resolve("output.mcpack");

        BedrockPacksGenerator.writeResourcePack(input.toFile(), output);

        try (ZipFile pack = new ZipFile(output.toFile())) {
            assertArrayEquals(direct, pack.getInputStream(pack.getEntry("biomes/direct.json")).readAllBytes());
            assertArrayEquals(extracted, pack.getInputStream(pack.getEntry("biomes/extracted.json")).readAllBytes());
        }
    }

    @Test
    void preservesDirectDefinitionsWithoutAnExtractedDirectory() throws Exception {
        final Path input = Files.createDirectory(this.directory.resolve("input"));
        final byte[] payload = {7, 8, 9};
        write(input.resolve("fogs/direct.json"), payload);
        final Path output = this.directory.resolve("output.mcpack");

        BedrockPacksGenerator.writeResourcePack(input.toFile(), output);

        try (ZipFile pack = new ZipFile(output.toFile())) {
            assertArrayEquals(payload, pack.getInputStream(pack.getEntry("fogs/direct.json")).readAllBytes());
            assertNull(pack.getEntry("biomes"));
        }
    }

    private static void write(final Path path, final byte[] payload) throws Exception {
        Files.createDirectories(path.getParent());
        Files.write(path, payload);
    }

}
