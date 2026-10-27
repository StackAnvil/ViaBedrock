/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2026 RK_01/RaphiMC and contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package net.raphimc.viabedrock.protocol.data;

import com.viaversion.nbt.tag.CompoundTag;
import net.raphimc.viabedrock.protocol.model.DimensionDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaDimensionRegistryTest {

    @Test
    void projectsServerBoundsAndCloudHeightWithoutReplacingOtherAttributes() {
        final var registry = registry();
        final var dimension = registry.getCompoundTag("minecraft:overworld");
        JavaRegistries.applyDimensionDefinition(dimension, new DimensionDefinition(-64, 384, 96, true));
        assertEquals(-64, dimension.getInt("min_y"));
        assertEquals(384, dimension.getInt("height"));
        assertEquals(384, dimension.getInt("logical_height"));
        final var attributes = dimension.getCompoundTag("attributes");
        assertEquals(96F, attributes.getFloat("minecraft:visual/cloud_height"));
        assertEquals("#ccffffff", attributes.getString("minecraft:visual/cloud_color"));
        assertEquals("#123456", attributes.getString("minecraft:visual/fog_color"));
    }

    @Test
    void suppressesCloudsWithTransparentColorAndAcceptsAnAbsentAttributeMap() {
        for (final boolean attributesPresent : new boolean[]{false, true}) {
            final var registry = registry();
            final var dimension = registry.getCompoundTag("minecraft:overworld");
            if (!attributesPresent) {
                dimension.remove("attributes");
            }
            JavaRegistries.applyDimensionDefinition(dimension, new DimensionDefinition(0, 256, 128, false));
            assertEquals("#00ffffff", dimension.getCompoundTag("attributes").getString("minecraft:visual/cloud_color"));
            assertEquals(128F, dimension.getCompoundTag("attributes").getFloat("minecraft:visual/cloud_height"));
        }
    }

    private static CompoundTag registry() {
        final var registry = new CompoundTag();
        final var dimension = new CompoundTag();
        final var attributes = new CompoundTag();
        attributes.putString("minecraft:visual/cloud_color", "#ccffffff");
        attributes.putString("minecraft:visual/fog_color", "#123456");
        dimension.put("attributes", attributes);
        registry.put("minecraft:overworld", dimension);
        return registry;
    }

}
