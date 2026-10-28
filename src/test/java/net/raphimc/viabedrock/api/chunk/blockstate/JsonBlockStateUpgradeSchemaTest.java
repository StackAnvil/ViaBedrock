/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2026 RK_01/RaphiMC and contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package net.raphimc.viabedrock.api.chunk.blockstate;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.libs.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

final class JsonBlockStateUpgradeSchemaTest {

    @Test
    void addsLegacyConnectionsWithoutReplacingCurrentConnections() throws Exception {
        final JsonBlockStateUpgradeSchema schema;
        try (var input = getClass().getResourceAsStream("/assets/viabedrock/block_state_upgrade_schema/0361_1.26.50_to_1.26.60.json")) {
            assertNotNull(input);
            schema = new JsonBlockStateUpgradeSchema(JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject());
        }
        for (final String name : new String[]{"minecraft:redstone_wire", "minecraft:fire", "minecraft:chorus_plant"}) {
            final var tag = new CompoundTag();
            tag.putString("name", name);
            tag.putInt("version", 1 << 24 | 21 << 16 | 60 << 8 | 33);
            final var states = new CompoundTag();
            final boolean redstone = name.equals("minecraft:redstone_wire");
            if (redstone) {
                states.putString("redstone_north", "up");
                states.putInt("redstone_signal", 12);
            } else {
                states.putByte("minecraft:connection_north", (byte) 1);
            }
            tag.put("states", states);
            schema.upgrade(tag);
            if (redstone) {
                assertEquals("up", states.getStringTag("redstone_north").getValue());
                assertEquals("none", states.getStringTag("redstone_east").getValue());
                assertEquals(12, states.getIntTag("redstone_signal").asInt());
            } else {
                assertEquals(1, states.getByteTag("minecraft:connection_north").asByte());
                assertEquals(0, states.getByteTag("minecraft:connection_east").asByte());
                assertEquals(name.equals("minecraft:chorus_plant"), states.contains("minecraft:connection_down"));
            }
            final var once = tag.copy();
            schema.upgrade(tag);
            assertEquals(once, tag);
        }
    }

}
