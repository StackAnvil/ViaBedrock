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

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PassengerOfBlockArguments_EmoteType;
import net.raphimc.viabedrock.protocol.model.PassengerOfBlock;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public final class PassengerOfBlockType extends Type<PassengerOfBlock> {

    public PassengerOfBlockType() {
        super(PassengerOfBlock.class);
    }

    @Override
    public PassengerOfBlock read(final ByteBuf buffer) {
        return new PassengerOfBlock(BedrockTypes.BLOCK_POSITION.read(buffer), BedrockTypes.POSITION_3F.read(buffer), buffer.readFloatLE(), buffer.readFloatLE(), PassengerOfBlockArguments_EmoteType.getByValue(buffer.readUnsignedByte()));
    }

    @Override
    public void write(final ByteBuf buffer, final PassengerOfBlock value) {
        BedrockTypes.BLOCK_POSITION.write(buffer, value.position());
        BedrockTypes.POSITION_3F.write(buffer, value.offset());
        buffer.writeFloatLE(value.rotation());
        buffer.writeFloatLE(value.rotationLimit());
        buffer.writeByte(value.emoteType().getValue());
    }

}
