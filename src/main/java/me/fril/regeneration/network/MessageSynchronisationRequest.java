package me.fril.regeneration.network;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import me.fril.regeneration.common.capability.CapabilityRegeneration;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSynchronisationRequest implements IMessage {
	
	private EntityPlayer player;
	
	public MessageSynchronisationRequest() {}
	
	public MessageSynchronisationRequest(EntityPlayer player) {
		this.player = player;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(player.dimension);
		ByteBufUtils.writeUTF8String(buf, player.getGameProfile().getId().toString());
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		int dim = buf.readInt();
		player = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dim).getPlayerEntityByUUID(UUID.fromString(ByteBufUtils.readUTF8String(buf)));
	}
	
	public static class Handler implements IMessageHandler<MessageSynchronisationRequest, IMessage> {
		
		@Override
		public IMessage onMessage(MessageSynchronisationRequest message, MessageContext ctx) {
			CapabilityRegeneration.getForPlayer(message.player).synchronise();
			return null;
		}
	}
	
}
