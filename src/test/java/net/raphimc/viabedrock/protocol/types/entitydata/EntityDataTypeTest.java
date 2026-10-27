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
package net.raphimc.viabedrock.protocol.types.entitydata;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class EntityDataTypeTest {

    @Test
    void readsBothDiscriminatorsAndLeavesTheFollowingFieldIntact() {
        final var buffer = Unpooled.wrappedBuffer(new byte[]{18, 0, 0, 42, 99});
        try {
            final var value = new EntityDataType().read(buffer);
            assertEquals(18, value.id());
            assertEquals(EntityDataTypesBedrock.BYTE, value.dataType());
            assertEquals(Byte.valueOf((byte) 42), value.<Byte>value());
            assertEquals(99, buffer.readUnsignedByte());
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }

    @Test
    void rejectsUnknownAndMismatchedTypesBeforeReadingTheValue() {
        for (final byte[] frame : new byte[][]{{18, 9, 9, 42}, {18, 1, 0, 42}}) {
            final var buffer = Unpooled.wrappedBuffer(frame);
            try {
                assertThrows(IllegalStateException.class, () -> new EntityDataType().read(buffer));
                assertEquals(42, buffer.readUnsignedByte());
            } finally {
                buffer.release();
            }
        }
    }

}
