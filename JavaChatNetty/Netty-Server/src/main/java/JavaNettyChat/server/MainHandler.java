package JavaNettyChat.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;

public class MainHandler extends SimpleChannelInboundHandler<String> {
    private static final List<Channel> channels = new ArrayList<>();
    private static int newClientIndex = 1;
    private String clientName;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Клиент подключен" + ctx);
        channels.add(ctx.channel());
        clientName = "Клиент #" + newClientIndex;
        newClientIndex ++;
        broadcastMessage("SERVER", "Подключился новый пользователь " + clientName + "\nСписок команд - /help");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        System.out.println("Получено сообщение от " + clientName + " : " + s);
        if (s.startsWith("/")){
            if(s.startsWith("/changename ")) {
                String newNickname = s.split("\\s", 2)[1];
                broadcastMessage("SERVER", "Пользователь " + clientName + " сменил имя на " + newNickname);
                clientName = newNickname;
            } else if (s.startsWith("/help")){
                broadcastMessage("SERVER", "Список команд :\n/help - список команд\n/changename +[новое имя] - установить новое имя");
            } else if (s.startsWith(("/nya"))){
                broadcastMessage("nya", "^-^");
            }
            return;
        }
        broadcastMessage(clientName, s);
    }

    public void broadcastMessage(String clientName, String message){
        String out = String.format("[%s]: %s\n", clientName, message);
        for (Channel c : channels){
            c.writeAndFlush(out);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Пользователь " + clientName + " вышел из сети");
        channels.remove(ctx.channel());
        broadcastMessage("SERVER", "Пользователь " + clientName + " вышел из сети");
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("Клиент " + clientName + " отвалился");
        channels.remove(ctx.channel());
        broadcastMessage("SERVER", "Клиент " + clientName + " вышел из сети");
        ctx.close();
    }
}
