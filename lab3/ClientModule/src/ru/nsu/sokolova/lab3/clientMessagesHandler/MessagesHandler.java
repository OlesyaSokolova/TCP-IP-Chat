package ru.nsu.sokolova.lab3.clientMessagesHandler;

import ru.nsu.sokolova.lab3.ancillaryPackage.consts.MessagesHandlerTypes;
import ru.nsu.sokolova.lab3.ancillaryPackage.messages.ClientMessage;
import ru.nsu.sokolova.lab3.ancillaryPackage.messages.ServerMessage;

import java.net.Socket;

public abstract class MessagesHandler implements Cloneable
{
    private MessagesHandlerTypes type_;
    protected Socket socket_;

    public MessagesHandler(MessagesHandlerTypes type)
    {
        type_ = type;
    }
    public abstract void setSocket(Socket socket);
    public abstract ServerMessage receiveMessage() throws Exception;
    public abstract void sendMessage(ClientMessage message) throws Exception;
    public MessagesHandler clone() throws CloneNotSupportedException
    {
        return (MessagesHandler)super.clone();
    }
    public abstract void finishWork();

}
