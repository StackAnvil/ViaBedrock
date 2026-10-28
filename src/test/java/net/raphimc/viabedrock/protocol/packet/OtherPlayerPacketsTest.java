/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2026 RK_01/RaphiMC and contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package net.raphimc.viabedrock.protocol.packet;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes26_3;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ClientboundPackets26_3;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.raphimc.viabedrock.api.model.entity.PlayerEntity;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.GameType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PassengerOfBlockArguments_EmoteType;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.PassengerOfBlocks;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import net.raphimc.viabedrock.protocol.types.item.NetworkItemStackDescriptorType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

final class OtherPlayerPacketsTest {

    @Test
    void preservesDeviceIdentityAndConsumesThePayloadOnce() throws Exception {
        // Initialize the component keys before VersionedTypes enters its dependent initializers.
        Class.forName(StructuredDataKey.class.getName());
        final var typeProtocol = mock(Protocol.class);
        final var mappingData = mock(MappingData.class);
        final var entityMappings = mock(FullMappings.class);
        when(typeProtocol.getMappingData()).thenReturn(mappingData);
        when(mappingData.getEntityMappings()).thenReturn(entityMappings);
        int nextId = 0;
        for (final var type : EntityTypes26_3.values()) {
            if (!type.isAbstractType()) {
                when(entityMappings.mappedId(type.identifier())).thenReturn(nextId++);
            }
        }
        when(entityMappings.mappedSize()).thenReturn(nextId);
        EntityTypes26_3.initialize(typeProtocol);
        for (final boolean seated : new boolean[]{false, true}) {
            check(seated, "", 0);
            check(seated, "5fa30b41-7d50-4b40-945b-977899244996", 7);
        }
    }

    private static void check(final boolean seated, final String deviceId, final int platform) throws Exception {
        final var protocol = mock(BedrockProtocol.class);
        OtherPlayerPackets.register(protocol);
        final var handler = ArgumentCaptor.forClass(PacketHandler.class);
        verify(protocol).registerClientbound(eq(ClientboundBedrockPackets.ADD_PLAYER), eq(ClientboundPackets26_3.ADD_ENTITY), handler.capture());

        final var user = mock(UserConnection.class);
        final var info = mock(ProtocolInfo.class);
        when(user.getProtocolInfo()).thenReturn(info);
        when(info.protocolVersion()).thenReturn(ProtocolVersion.v1_21);
        when(info.serverProtocolVersion()).thenReturn(ProtocolVersion.v1_21);
        final var session = mock(GameSessionStorage.class);
        final var items = mock(ItemRewriter.class);
        final var entities = mock(EntityTracker.class);
        final var player = mock(PlayerEntity.class);
        final var itemType = new NetworkItemStackDescriptorType(-1, new Int2ObjectOpenHashMap<>(), false);
        when(user.get(GameSessionStorage.class)).thenReturn(session);
        when(user.get(ItemRewriter.class)).thenReturn(items);
        when(user.get(EntityTracker.class)).thenReturn(entities);
        when(session.getLevelGameType()).thenReturn(GameType.Survival);
        when(items.newItemType()).thenReturn(itemType);
        when(items.javaItem(any(BedrockItem.class))).thenReturn(StructuredItem.empty());
        when(entities.addEntity(any(PlayerEntity.class))).thenReturn(player);

        final ByteBuf buffer = Unpooled.buffer();
        try {
            BedrockTypes.UUID.write(buffer, UUID.fromString("8695b182-22a8-4c88-b4ba-07d33e903c28"));
            BedrockTypes.STRING.write(buffer, "OtherPlayer");
            BedrockTypes.UNSIGNED_VAR_LONG.write(buffer, 42L);
            BedrockTypes.STRING.write(buffer, "platform-chat");
            BedrockTypes.POSITION_3F.write(buffer, new Position3f(1F, 65F, -2F));
            BedrockTypes.POSITION_3F.write(buffer, Position3f.ZERO);
            BedrockTypes.POSITION_3F.write(buffer, Position3f.ZERO);
            itemType.write(buffer, new BedrockItem((short) 0));
            BedrockTypes.VAR_INT.write(buffer, GameType.Survival.getValue());
            buffer.writeByte(0); // metadata count
            buffer.writeByte(0); // integer property count
            buffer.writeByte(0); // float property count
            buffer.writeLongLE(42L).writeByte(0).writeByte(0).writeByte(0); // abilities, no layers
            buffer.writeByte(0); // actor link count
            BedrockTypes.OPTIONAL_PASSENGER_OF_BLOCKS.write(buffer, seated
                ? new PassengerOfBlocks(new BlockPosition(-2, 64, 3), new Position3f(0F, 0.5F, 0F), 90F, 45F, PassengerOfBlockArguments_EmoteType.Riding)
                : null);
            BedrockTypes.STRING.write(buffer, deviceId);
            buffer.writeIntLE(platform);

            final var input = spy(new PacketWrapperImpl(ClientboundPackets26_3.ADD_ENTITY, buffer, user));
            doNothing().when(input).send(any());
            final var profile = spy(new PacketWrapperImpl(ClientboundPackets26_3.PLAYER_INFO_UPDATE, null, user));
            doNothing().when(profile).send(any());
            try (var packets = mockStatic(PacketWrapper.class)) {
                packets.when(() -> PacketWrapper.create(ClientboundPackets26_3.PLAYER_INFO_UPDATE, user)).thenReturn(profile);
                packets.when(() -> PacketWrapper.create(ClientboundPackets26_3.SET_EQUIPMENT, user)).thenReturn(mock(PacketWrapper.class));
                handler.getValue().handle(input);
            }

            assertFalse(buffer.isReadable());
            final GameProfile.Property[] expected = {
                new GameProfile.Property("platform_online_id", "platform-chat"),
                new GameProfile.Property("device_id", deviceId),
                new GameProfile.Property("device_os", Integer.toString(platform))
            };
            assertArrayEquals(expected, profile.get(Types.PROFILE_PROPERTY_ARRAY, 0));
            verify(input).send(BedrockProtocol.class);
            verify(profile).send(BedrockProtocol.class);
            verify(player).sendInitialEntityData();
        } finally {
            buffer.release();
        }
    }

}
