/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2026 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package net.raphimc.viabedrock.protocol.types.model;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerEnumName;
import net.raphimc.viabedrock.protocol.model.FullContainerName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class FullContainerNameTypeTest {

    @Test
    void containerNamesUseProtocol2225SlotIds() {
        final FullContainerNameType type = new FullContainerNameType();
        final Map<ContainerEnumName, Integer> ids = Map.of(
                ContainerEnumName.InventoryContainer, 29,
                ContainerEnumName.CursorContainer, 59,
                ContainerEnumName.CreatedOutputContainer, 60,
                ContainerEnumName.RecipeFoodContainer, 64,
                ContainerEnumName.RecipeBlocksContainer, 65,
                ContainerEnumName.RecipeFurnaceItemsContainer, 66);

        ids.forEach((name, id) -> {
            final ByteBuf buffer = Unpooled.buffer();
            try {
                type.write(buffer, new FullContainerName(name, null));
                assertArrayEquals(new byte[] {id.byteValue(), 0}, ByteBufUtil.getBytes(buffer));
                assertSame(name, ContainerEnumName.getByValue(id));
            } finally {
                buffer.release();
            }
        });
    }

}
