package ru.nsu.sokolova.lab3.client;

import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ServerMessagesDataTypes;
import ru.nsu.sokolova.lab3.ancillaryPackage.consts.MessagesHandlerTypes;
import ru.nsu.sokolova.lab3.clientMessagesHandler.MessagesHandler;
import ru.nsu.sokolova.lab3.clientMessagesHandler.MsgHandlersFactory;
import ru.nsu.sokolova.lab3.ancillaryPackage.messages.ClientMessage;
import ru.nsu.sokolova.lab3.ancillaryPackage.messages.ServerMessage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;


import static ru.nsu.sokolova.lab3.ancillaryPackage.consts.MessagesConsts.*;
import static ru.nsu.sokolova.lab3.ancillaryPackage.consts.SettingsConsts.*;

public class ClientConnection
{
    private int sessionID_;
    private Socket serverSocket_;
    public ClientWindow.ClientListener listener_;
    public Thread threadListener_;
    private MessagesHandler msgHandler_;

    public ClientConnection(ClientWindow.ClientListener clientListener,  String IPAddress, int port) throws IOException, CloneNotSupportedException {

        listener_ = clientListener;
        try {
            serverSocket_ = new Socket(IPAddress, port);
            FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH);
            Properties settings_;
            settings_ = new Properties();
            settings_.load(fis);
            MessagesHandlerTypes msgHandlerType =  MessagesHandlerTypes.valueOf(settings_.getProperty("messagesHandlerType"));
            MsgHandlersFactory handlersFactory = new MsgHandlersFactory();
            msgHandler_ = handlersFactory.createHandler(msgHandlerType);
            msgHandler_.setSocket(serverSocket_);
        }
        catch(java.net.ConnectException ex)
        {
            throw ex;
        }
        catch (IOException | CloneNotSupportedException e) {
            throw e;
        }
        threadListener_ = new Thread(() -> {
            while(!serverSocket_.isClosed() && !threadListener_.isInterrupted())
            {
                ServerMessage messageFromServer = null;
                try {
                    messageFromServer = msgHandler_.receiveMessage();
                }
                catch(NullPointerException ex)
                {
                   break;
                }
                catch(EOFException |SocketException ex)
                {
                    messageFromServer = new ServerMessage(ServerMessagesDataTypes.ERROR, INTERRUPTED_CONNECTION);
                }
                catch(Exception ex)
                {
                    messageFromServer = new ServerMessage(ServerMessagesDataTypes.ERROR, ex.toString());
                }

                if(messageFromServer.getType().equals(ServerMessagesDataTypes.ERROR))
                {
                    if(messageFromServer.getData().equals(TIMEOUT_FOR_CLIENT) || messageFromServer.getData().equals(INTERRUPTED_CONNECTION))
                    {
                        listener_.acceptMessage(messageFromServer);
                        this.disconnect();
                        return;
                    }
                }
                if(messageFromServer.getType().equals(ServerMessagesDataTypes.SESSION_ID))
                {
                    ClientConnection.this.sessionID_ = Integer.valueOf(messageFromServer.getData());
                    continue;
                }
                listener_.acceptMessage(messageFromServer);
            }
        });
    }

    public void startConversation()
    {
        threadListener_.start();
    }
    public void sendMessage(ClientMessage message)
    {
        try {
            msgHandler_.sendMessage(message);
        } catch (Exception ex) {
            listener_.printClientError(ex.toString());
        }
    }

    public void disconnect()
    {
        try {
            msgHandler_.finishWork();
            serverSocket_.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        threadListener_.interrupt();
    }

    public int getSessionID() {
        return sessionID_;
    }
}
