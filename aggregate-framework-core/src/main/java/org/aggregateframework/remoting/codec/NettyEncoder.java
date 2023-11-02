package org.aggregateframework.remoting.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.aggregateframework.remoting.protocol.RemotingCommand;
import org.aggregateframework.transaction.serializer.RemotingCommandSerializer;

public class NettyEncoder extends MessageToByteEncoder<RemotingCommand> {

    RemotingCommandSerializer serializer;

    public NettyEncoder(RemotingCommandSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, RemotingCommand message,
                          ByteBuf out) throws Exception {
        out.writeBytes(serializer.serialize(message));
        ctx.flush();
    }
}
