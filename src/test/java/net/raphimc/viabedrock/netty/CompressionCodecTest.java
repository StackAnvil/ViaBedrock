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
package net.raphimc.viabedrock.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PacketCompressionAlgorithm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class CompressionCodecTest {

    @Test
    void consumesAnUncompressedFrameWithoutDecodingItsPayloadAgain() {
        final var channel = new EmbeddedChannel(new CompressionCodec(PacketCompressionAlgorithm.None, 0));
        try {
            assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{(byte) 0xFF, 1, 3, 5, 7})));
            final ByteBuf decoded = channel.readInbound();
            try {
                assertEquals(4, decoded.readableBytes());
                assertEquals(0x01030507, decoded.readInt());
            } finally {
                decoded.release();
            }
            assertNull(channel.readInbound());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void readsTheByteFrameSentinelAfterNegotiatingTheUnsignedShortSentinel() {
        final ByteBuf settings = Unpooled.wrappedBuffer(new byte[]{(byte) 0xFF, (byte) 0xFF});
        final PacketCompressionAlgorithm negotiated = PacketCompressionAlgorithm.getByValue(settings.readUnsignedShortLE());
        settings.release();
        final var channel = new EmbeddedChannel(new CompressionCodec(negotiated, 0));
        try {
            assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{(byte) 0xFF, 1, 3, 5, 7})));
            final ByteBuf decoded = channel.readInbound();
            try {
                assertEquals(4, decoded.readableBytes());
                assertEquals(0x01030507, decoded.readInt());
            } finally {
                decoded.release();
            }
            assertTrue(channel.writeOutbound(Unpooled.wrappedBuffer(new byte[]{2, 4, 6, 8})));
            final ByteBuf encoded = channel.readOutbound();
            try {
                assertEquals(5, encoded.readableBytes());
                assertEquals(0xFF, encoded.readUnsignedByte());
                assertEquals(0x02040608, encoded.readInt());
            } finally {
                encoded.release();
            }
        } finally {
            channel.finishAndReleaseAll();
        }
    }

}
