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

/** Normalizes legacy connected blocks whose state version did not change in 26.60. */
public final class ConnectionBlockStateUpgradeSchema extends BlockStateUpgradeSchema {

    public ConnectionBlockStateUpgradeSchema() {
        super(1, 21, 60, 33);
        this.actions.add(tag -> {
            if (!(tag.get("states") instanceof CompoundTag states)) {
                return;
            }
            final String name = tag.getStringTag("name").getValue();
            if (name.equals("minecraft:redstone_wire")) {
                for (String side : new String[]{"north", "east", "south", "west"}) {
                    if (!states.contains("redstone_" + side)) {
                        states.putString("redstone_" + side, "none");
                    }
                }
            } else if (name.equals("minecraft:chorus_plant") || name.equals("minecraft:fire")) {
                for (String side : new String[]{"north", "east", "south", "west", "up", "down"}) {
                    if (side.equals("down") && name.equals("minecraft:fire")) {
                        continue;
                    }
                    if (!states.contains("minecraft:connection_" + side)) {
                        states.putByte("minecraft:connection_" + side, (byte) 0);
                    }
                }
            }
        });
    }

}
