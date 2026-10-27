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
package net.raphimc.viabedrock.protocol.types.model;

import com.viaversion.viaversion.api.minecraft.BlockPosition;
import io.netty.buffer.Unpooled;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PassengerOfBlockArguments_EmoteType;
import net.raphimc.viabedrock.protocol.model.PassengerOfBlock;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class PassengerOfBlockTypeTest {

    @Test
    void absentPassengerLeavesTheFollowingSpawnFieldIntact() {
        final var buffer = Unpooled.buffer();
        try {
            buffer.writeBoolean(false).writeIntLE(0x13579BDF);
            assertNull(BedrockTypes.OPTIONAL_PASSENGER_OF_BLOCK.read(buffer));
            assertEquals(0x13579BDF, buffer.readIntLE());
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }

    @Test
    void readsSignedBlockCoordinatesAndTheCompletePresentPassenger() {
        final var buffer = Unpooled.buffer();
        final var encoded = Unpooled.buffer();
        try {
            // Protocol 2225: presence, three signed varints, five little-endian floats, emote byte.
            buffer.writeBytes(new byte[]{1, 3, 127, (byte) 0x80, 2});
            buffer.writeFloatLE(-1.25F).writeFloatLE(0.5F).writeFloatLE(2.75F);
            buffer.writeFloatLE(270F).writeFloatLE(45F).writeByte(2);
            final int end = buffer.writerIndex();
            buffer.writeIntLE(0x13579BDF);

            final PassengerOfBlock actual = BedrockTypes.OPTIONAL_PASSENGER_OF_BLOCK.read(buffer);
            assertEquals(new PassengerOfBlock(new BlockPosition(-2, -64, 128), new Position3f(-1.25F, 0.5F, 2.75F), 270F, 45F, PassengerOfBlockArguments_EmoteType.Laying), actual);
            assertEquals(end, buffer.readerIndex());
            assertEquals(0x13579BDF, buffer.readIntLE());
            BedrockTypes.OPTIONAL_PASSENGER_OF_BLOCK.write(encoded, actual);
            assertEquals(buffer.slice(0, end), encoded);
        } finally {
            buffer.release();
            encoded.release();
        }
    }

}
