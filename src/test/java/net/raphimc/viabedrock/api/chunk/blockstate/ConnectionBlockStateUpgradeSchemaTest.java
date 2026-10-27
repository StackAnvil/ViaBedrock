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
package net.raphimc.viabedrock.api.chunk.blockstate;

import com.viaversion.nbt.tag.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class ConnectionBlockStateUpgradeSchemaTest {

    @Test
    void addsMissingConnectionsWithoutErasingNewStatesAtTheUnchangedVersion() {
        final var schema = new ConnectionBlockStateUpgradeSchema();
        final var tag = new CompoundTag();
        tag.putString("name", "minecraft:redstone_wire");
        tag.putInt("version", schema.version());
        final var states = new CompoundTag();
        states.putInt("redstone_signal", 12);
        states.putString("redstone_east", "up");
        tag.put("states", states);

        schema.upgrade(tag);
        assertEquals("up", states.getStringTag("redstone_east").getValue());
        assertEquals("none", states.getStringTag("redstone_north").getValue());
        assertEquals("none", states.getStringTag("redstone_south").getValue());
        assertEquals("none", states.getStringTag("redstone_west").getValue());
        assertEquals(12, states.getIntTag("redstone_signal").asInt());
        final var once = tag.copy();
        schema.upgrade(tag);
        assertEquals(once, tag);
    }

    @Test
    void keepsFireAndChorusConnectionDomainsDistinct() {
        for (String name : new String[]{"minecraft:fire", "minecraft:chorus_plant"}) {
            final var tag = new CompoundTag();
            tag.putString("name", name);
            final var states = new CompoundTag();
            states.putByte("minecraft:connection_up", (byte) 1);
            tag.put("states", states);
            new ConnectionBlockStateUpgradeSchema().upgrade(tag);
            assertEquals((byte) 1, states.getByteTag("minecraft:connection_up").asByte());
            assertEquals(name.equals("minecraft:chorus_plant"), states.contains("minecraft:connection_down"));
            assertEquals((byte) 0, states.getByteTag("minecraft:connection_north").asByte());
        }
    }

}
