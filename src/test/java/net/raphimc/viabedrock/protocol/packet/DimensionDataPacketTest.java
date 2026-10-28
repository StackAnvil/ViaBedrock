/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2026 RK_01/RaphiMC and contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package net.raphimc.viabedrock.protocol.packet;

import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DimensionDataPacketTest {

    @Test
    void retainsBoundsAndCloudSettingsWithoutConsumingTheNextDefinition() {
        for (final int[] range : new int[][]{{0, 256}, {-64, 384}, {32, 64}}) {
            for (final boolean clouds : new boolean[]{false, true}) {
                final ByteBuf buffer = definition(range[0], range[1], -32, clouds);
                try {
                    buffer.writeByte(77);
                    final var wrapper = new PacketWrapperImpl(ClientboundBedrockPackets.DIMENSION_DATA, buffer, null);
                    final var decoded = JoinPackets.readDimensionDefinition(wrapper);
                    assertEquals(range[0], decoded.minimumY());
                    assertEquals(range[1], decoded.height());
                    assertEquals(range[0] + range[1], decoded.maximumY());
                    assertEquals(-32, decoded.cloudHeight());
                    assertEquals(clouds, decoded.renderClouds());
                    assertEquals(77, buffer.readUnsignedByte());
                    assertFalse(buffer.isReadable());
                } finally {
                    buffer.release();
                }
            }
        }
    }

    @Test
    void rejectsEmptyAndNegativeHeightRanges() {
        for (final int range : new int[]{0, -16, Integer.MIN_VALUE}) {
            final ByteBuf buffer = definition(-64, range, 192, true);
            try {
                final var wrapper = new PacketWrapperImpl(ClientboundBedrockPackets.DIMENSION_DATA, buffer, null);
                assertThrows(IllegalArgumentException.class, () -> JoinPackets.readDimensionDefinition(wrapper));
            } finally {
                buffer.release();
            }
        }
    }

    @Test
    void rejectsUpperBoundOverflowInsteadOfWrappingTheDimension() {
        final ByteBuf buffer = definition(Integer.MAX_VALUE - 15, 16, 0, false);
        try {
            final var wrapper = new PacketWrapperImpl(ClientboundBedrockPackets.DIMENSION_DATA, buffer, null);
            assertThrows(ArithmeticException.class, () -> JoinPackets.readDimensionDefinition(wrapper));
        } finally {
            buffer.release();
        }
    }

    @Test
    void retainsRepresentableBoundsAtTheSignedIntegerLimits() {
        for (final int minimum : new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE - 16}) {
            final ByteBuf buffer = definition(minimum, 16, 128, true);
            try {
                final var wrapper = new PacketWrapperImpl(ClientboundBedrockPackets.DIMENSION_DATA, buffer, null);
                final var decoded = JoinPackets.readDimensionDefinition(wrapper);
                assertEquals(minimum, decoded.minimumY());
                assertEquals(minimum + 16, decoded.maximumY());
            } finally {
                buffer.release();
            }
        }
    }

    private static ByteBuf definition(final int minimum, final int range, final int cloudHeight, final boolean clouds) {
        final ByteBuf buffer = Unpooled.buffer();
        BedrockTypes.VAR_INT.writePrimitive(buffer, minimum);
        BedrockTypes.VAR_INT.writePrimitive(buffer, range);
        BedrockTypes.VAR_INT.writePrimitive(buffer, 1); // generator
        BedrockTypes.VAR_INT.writePrimitive(buffer, 0); // dimension type
        BedrockTypes.UUID.write(buffer, UUID.randomUUID());
        BedrockTypes.STRING.write(buffer, "minecraft:plains");
        BedrockTypes.VAR_INT.writePrimitive(buffer, cloudHeight);
        buffer.writeBoolean(clouds);
        return buffer;
    }

}
