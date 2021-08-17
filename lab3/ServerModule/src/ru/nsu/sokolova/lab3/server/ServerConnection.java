package ru.nsu.sokolova.lab3.server;

import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ClientMessagesDataTypes;
import ru.nsu.sokolova.lab3.ancillaryPackage.consts.MessagesHandlerTypes;
import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ServerMessagesDataTypes;
import ru.nsu.sokolova.lab3.ancillaryPackage.messages.ClientMessage;
import ru.nsu.sokolova.lab3.ancillaryPackage.messages.ServerMessage;
import ru.nsu.sokolova.lab3.serverMessagesHandler.MessagesHandler;
import ru.nsu.sokolova.lab3.serverMessagesHandler.MsgHandlersFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;

import static ru.nsu.sokolova.lab3.ancillaryPackage.consts.MessagesConsts.*;
import static ru.nsu.sokolova.lab3.ancillaryPackage.consts.SettingsConsts.*;
import static ru.nsu.sokolova.lab3.ConnectionConsts.SHOW_TEXT;

public class ServerConnection implements Runnable
{
    private Properties settings_;
    private Socket clientSocket_;
    private String connectionName_;
    private ChatServer.ServerAssistant serverAssistant_;
    private MessagesHandler msgHandler_;
    private int sessionID_;
    private void loadSettings() {
        try {
            FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH);
            settings_ = new Properties();
            settings_.load(fis);
        } catch (IOException ex) {
            System.out.println(NO_CONFIG_FILE);
        }
    }
    public ServerConnection(ChatServer.ServerAssistant serverAssistant, Socket clientSocket) throws CloneNotSupportedException {
        clientSocket_ = clientSocket;
        serverAssistant_ = serverAssistant;
        loadSettings();
        try {
            MessagesHandlerTypes msgHandlerType = MessagesHandlerTypes.valueOf(settings_.getProperty("messagesHandlerType"));
            MsgHandlersFactory handlersFactory = new MsgHandlersFactory();
            msgHandler_ = handlersFactory.createHandler(msgHandlerType);
            msgHandler_.setSocket(clientSocket_);
        } catch (CloneNotSupportedException ex) {
            throw ex;
        }
    }
    public void sendMessage(ServerMessage message)
    {
        try {
            msgHandler_.sendMessage(message);
        }
        catch (Exception e) {
            serverAssistant_.logError(e.toString());
        }
    }
    public void disconnect() {
        serverAssistant_.disconnectClient(ServerConnection.this.sessionID_);
        try
        {
            msgHandler_.finishWork();
            clientSocket_.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public String getName()
    {
        return  connectionName_;
    }
    void setSessionID(int ID)
    {
        sessionID_ = ID;
    }

    public int getSessionID() {
        return sessionID_;
    }

    @Override
    public void run() {
        if(!clientSocket_.isClosed())
        {
            ServerMessage serverMessage = null;
            ClientMessage messageFromClient = null;
            try {
                messageFromClient = msgHandler_.receiveMessage();
            }
            catch(java.net.SocketTimeoutException ex)
            {
                serverAssistant_.logInfo("Server disconnected client" + WORDS_DELIMITER + connectionName_ + WORDS_DELIMITER + "because of timeout.");
                serverMessage = new ServerMessage(ServerMessagesDataTypes.ERROR, TIMEOUT_FOR_CLIENT);
                try {
                    msgHandler_.sendMessage(serverMessage);
                }catch (Exception e) {
                    serverAssistant_.logError(e.toString());
                }
                this.disconnect();
                serverAssistant_.informOthers(ServerConnection.this, new ServerMessage(ServerMessagesDataTypes.MESSAGE, connectionName_ + SHOW_TEXT + TIMEOUT_FOR_SERVER));
                return;
            }
            catch(EOFException | SocketException ex)
            {
                serverAssistant_.logInfo("Client disconnected" + SHOW_TEXT + connectionName_);
                serverMessage = new ServerMessage(ServerMessagesDataTypes.USERLOGOUT, connectionName_ + WORDS_DELIMITER + CLIENT_LEAVED_CHAT);
                serverAssistant_.informOthers(ServerConnection.this, serverMessage);
                this.disconnect();
                return;
            }
            catch(Exception ex)
            {
                serverMessage = new ServerMessage(ServerMessagesDataTypes.ERROR, ex.toString());
                try {
                    msgHandler_.sendMessage(serverMessage);
                } catch (Exception e) {
                    serverAssistant_.logError(e.toString());
                }
                return;
            }
            if(messageFromClient.getData().equals(null))
            {
                serverMessage = new ServerMessage(ServerMessagesDataTypes.ERROR, NO_DATA_RECEIVED);
                try {
                    msgHandler_.sendMessage(serverMessage);
                } catch (Exception e) {
                    serverAssistant_.logError(e.toString());
                }
                this.disconnect();
                return;
            }
            if(messageFromClient.getType().equals(ClientMessagesDataTypes.LOGIN))
            {
                connectionName_ = messageFromClient.getData();
                try {
                    msgHandler_.sendMessage(new ServerMessage(ServerMessagesDataTypes.SESSION_ID, String.valueOf(ServerConnection.this.sessionID_)));
                    msgHandler_.sendMessage(new ServerMessage(ServerMessagesDataTypes.HISTORY, serverAssistant_.getHistory()));
                }
                catch (Exception e) {
                    serverAssistant_.logError(e.toString());
                }
                serverAssistant_.logInfo("New client connected" + SHOW_TEXT + connectionName_);
                serverAssistant_.addClient(ServerConnection.this);
                serverMessage = new ServerMessage(ServerMessagesDataTypes.USERLOGIN, messageFromClient.getData() + WORDS_DELIMITER+ CLIENT_JOINED_CHAT);
                serverAssistant_.informOthers(ServerConnection.this, serverMessage);
            }
            else if(messageFromClient.getType().equals(ClientMessagesDataTypes.LOGOUT))
            {
                this.disconnect();
                serverAssistant_.logInfo("Client disconnected" + SHOW_TEXT + connectionName_);
                serverAssistant_.informOthers(ServerConnection.this, new ServerMessage(ServerMessagesDataTypes.USERLOGOUT, connectionName_ + WORDS_DELIMITER + CLIENT_LEAVED_CHAT));
                return;
            }
            else if(messageFromClient.getType().equals(ClientMessagesDataTypes.MESSAGE))
            {
                serverAssistant_.logInfo("Message from client" + WORDS_DELIMITER + connectionName_ + SHOW_TEXT + messageFromClient.getData());
                serverAssistant_.sendToClient(ServerConnection.this, new ServerMessage(ServerMessagesDataTypes.NO_DATA_SUCCESS, null));
                serverAssistant_.sendToEveryone(new ServerMessage(ServerMessagesDataTypes.MESSAGE, connectionName_+ SHOW_TEXT + messageFromClient.getData()));
            }
        }
        return;
    }
}
