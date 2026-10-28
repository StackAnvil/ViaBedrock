/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2026 RK_01/RaphiMC and contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package net.raphimc.viabedrock.api.resourcepack;

import net.raphimc.viabedrock.api.resourcepack.content.ZipContent;
import net.raphimc.viabedrock.api.resourcepack.definition.BiomeDefinitions;
import net.raphimc.viabedrock.api.resourcepack.definition.FogDefinitions;
import net.raphimc.viabedrock.api.resourcepack.definition.RenderControllerDefinitions;
import net.raphimc.viabedrock.protocol.storage.ResourcePackStorage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

final class VanillaResourcePackTest {

    @Test
    void loadsBiomeFogAndControllerDefinitionsFromBundledPacks() throws Exception {
        final var packs = new ArrayList<ResourcePack>();
        for (final String name : new String[]{"vanilla", "vanilla_1.16.100", "vanilla_1.19.0"}) {
            try (var input = getClass().getResourceAsStream("/assets/viabedrock/resource_packs/" + name + ".mcpack")) {
                assertNotNull(input);
                packs.add(new ResourcePack(new ZipContent(input.readAllBytes())));
            }
        }
        final var storage = mock(ResourcePackStorage.class);
        when(storage.getPackStackBottomToTop()).thenReturn(packs);
        assertFalse(new BiomeDefinitions(storage).biomes().isEmpty());
        assertNotNull(new FogDefinitions(storage).get("minecraft:fog_stone_beach"));
        assertFalse(new RenderControllerDefinitions(storage).renderControllers().isEmpty());
    }

}
