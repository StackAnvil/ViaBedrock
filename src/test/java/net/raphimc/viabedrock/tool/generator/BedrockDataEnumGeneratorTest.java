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
package net.raphimc.viabedrock.tool.generator;

import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class BedrockDataEnumGeneratorTest {

    @Test
    void fillsOmittedPacketIdsInNumericOrderWithoutReplacingEnumAliases() {
        final JsonObject packetEnum = packetEnum();
        final var missing = BedrockDataEnumGenerator.missingPacketIds(packetEnum, List.of(
            packet("LastPacket", 357, 2225), packet("LoginPacket", 1, 2225),
            packet("FirstPacket", 327, 2225)));
        assertEquals(List.of("FirstPacket", "LastPacket"), List.copyOf(missing.keySet()));
        assertEquals(Map.of("FirstPacket", 327, "LastPacket", 357), missing);
        assertEquals(packetEnum(), packetEnum);
    }

    @Test
    void rejectsConflictingSchemaIdsAndEnumNames() {
        assertThrows(IllegalStateException.class, () -> BedrockDataEnumGenerator.missingPacketIds(
            packetEnum(), List.of(packet("OnePacket", 327, 2225), packet("TwoPacket", 327, 2225))));
        assertThrows(IllegalStateException.class, () -> BedrockDataEnumGenerator.missingPacketIds(
            packetEnum(), List.of(packet("Login", 327, 2225))));
    }

    @Test
    void rejectsMixedProtocolSnapshotsAndMissingExplicitValues() {
        assertThrows(IllegalStateException.class, () -> BedrockDataEnumGenerator.missingPacketIds(
            packetEnum(), List.of(packet("NewPacket", 327, 2193))));
        final JsonObject packetEnum = packetEnum();
        packetEnum.remove("x-enum-binary-value");
        assertThrows(IllegalStateException.class, () -> BedrockDataEnumGenerator.missingPacketIds(packetEnum, List.of()));
    }

    private static JsonObject packetEnum() {
        return JsonParser.parseString("{\"enum\":[\"Login\"],\"x-enum-binary-value\":[1],\"x-protocol-version\":2225}").getAsJsonObject();
    }

    private static JsonObject packet(final String name, final int id, final int protocol) {
        final JsonObject packet = new JsonObject();
        packet.addProperty("title", name);
        packet.addProperty("x-protocol-version", protocol);
        final JsonObject properties = new JsonObject();
        properties.addProperty("[cereal:packet]", id);
        packet.add("$metaProperties", properties);
        return packet;
    }

}
