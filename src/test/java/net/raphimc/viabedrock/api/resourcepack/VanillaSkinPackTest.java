/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2026 RK_01/RaphiMC and contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package net.raphimc.viabedrock.api.resourcepack;

import net.raphimc.viabedrock.api.resourcepack.content.ZipContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class VanillaSkinPackTest {

    @Test
    void loadsTheBundledSkinAndGeometryUsedDuringLogin() throws Exception {
        try (var input = getClass().getResourceAsStream("/assets/viabedrock/skin_packs/vanilla.mcpack")) {
            assertNotNull(input);
            final var pack = new ResourcePack(new ZipContent(input.readAllBytes()));
            final var image = pack.content().getImage("steve.png").getImage();
            final var geometry = pack.content().getSortedJson("geometry.json").getAsJsonArray("minecraft:geometry").asList().stream()
                    .map(element -> element.getAsJsonObject().getAsJsonObject("description"))
                    .filter(description -> description.get("identifier").getAsString().equals("geometry.humanoid.custom"))
                    .findFirst().orElseThrow();
            assertEquals(image.getWidth(), geometry.get("texture_width").getAsInt());
            assertEquals(image.getHeight(), geometry.get("texture_height").getAsInt());
        }
    }

}
