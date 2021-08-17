package ru.nsu.sokolova.lab3.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

import ru.nsu.sokolova.lab3.ServerConsts;
import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ServerMessagesDataTypes;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import ru.nsu.sokolova.lab3.ancillaryPackage.messages.ServerMessage;

import static ru.nsu.sokolova.lab3.ancillaryPackage.consts.MessagesConsts.NO_DATA;
import static ru.nsu.sokolova.lab3.ancillaryPackage.consts.SettingsConsts.*;
import static ru.nsu.sokolova.lab3.ConnectionConsts.*;

public class ChatServer
{
    private ServerAssistant serverAssistant_;
    private HashMap<Integer, ServerConnection> connections_;
    private Properties settings_;
    private int serverPort_;
    private Logger logger_;
    private ArrayList<String> history_;
    private ScheduledThreadPoolExecutor executor_;

    class ServerAssistant
    {
        void informOthers(ServerConnection connection,  ServerMessage message)
        {
            saveMessage(message.getData());
            for(Map.Entry entry : connections_.entrySet())
            {
                if(entry.getValue() != connection)
                {
                    ((ServerConnection)entry.getValue()).sendMessage(message);
                }
            }
        }
        void logInfo(String event)
        {
            logger_.info(event);
        }
        void addClient(ServerConnection newConnection)
        {
            int ID = ChatServer.this.newSessionID();
            newConnection.setSessionID(ID);
            connections_.put(ID, newConnection);
            String participantsList  =  getParticipantslist();
            sendToAllConnections(new ServerMessage(ServerMessagesDataTypes.LISTUSERS, participantsList));
        }
        void sendToEveryone(ServerMessage message)
        {
            saveMessage(message.getData());
            ChatServer.this.sendToAllConnections(message);
        }
        void disconnectClient(int sessionID)
        {
            executor_.remove(connections_.get(sessionID));
            connections_.remove(sessionID);
            String participantsList  =  getParticipantslist();
            sendToAllConnections(new ServerMessage(ServerMessagesDataTypes.LISTUSERS, participantsList));
        }
        String getHistory()
        {
            String result = new String();
            if(history_.size() == 0)
            {
                return NO_DATA;
            }
            for (Iterator<String> iterator = history_.iterator(); iterator.hasNext();)
            {
                String message = iterator.next();
                result += (message + LINES_DELIMITER);
            }
            result = result.substring(0, result.length() - LINES_DELIMITER.length());
            return  result;
        }

        public void sendToClient(ServerConnection connection, ServerMessage message)
        {
            connection.sendMessage(message);
        }

        public void logError(String error)
        {
            logger_.error(error);
        }

        public String getParticipantslist()
        {
            return  ChatServer.this.getParticipantslist();
        }
    };

    private void loadSettings() {
        try {
            PropertyConfigurator.configure(ServerConsts.LOGGING_SETTINGS_FILE_PATH);
            logger_ = Logger.getLogger(ChatServer.class);
            FileInputStream settingsFile = new FileInputStream(CONFIG_FILE_PATH);
            settings_ = new Properties();
            settings_.load(settingsFile);
        } catch (IOException ex) {
            logger_.error(NO_CONFIG_FILE);
        }
    }
    ChatServer()
    {
        loadSettings();
    }

    void startWorking()
    {
        serverPort_ =  Integer.valueOf(settings_.getProperty("serverPort"));
        connections_ = new HashMap<>();
        history_ = new ArrayList<>();
        serverAssistant_ = new ServerAssistant();
        int maxThreadsNumber = Integer.valueOf(settings_.getProperty("maxClients"));
        executor_ = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(maxThreadsNumber);
        logger_.info(ServerConsts.SERVER_STRARTED_OK);
        try (ServerSocket serverSocket = new ServerSocket(serverPort_))
        {
            int timeout =  Integer.valueOf(settings_.getProperty("timeout"));
            while(!serverSocket.isClosed())
            {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(timeout*MILLISEC_IN_SEC);
                ServerConnection newConn = new ServerConnection(serverAssistant_, clientSocket);
                executor_.scheduleAtFixedRate(newConn, INIT_DELAY, PERIOD, TimeUnit.MILLISECONDS );
            }
        }
        catch(IOException | CloneNotSupportedException e)
        {
            logger_.error(e.toString());
        }
        catch(Exception ex)
        {
            logger_.error(ex.toString());
        }
    }

    private void sendToAllConnections(ServerMessage message)
    {
        for(Map.Entry entry : connections_.entrySet())
        {
            ((ServerConnection)entry.getValue()).sendMessage(message);
        }
    }

    public void saveMessage(String message)
    {
        int historySize =  Integer.valueOf(settings_.getProperty("histroryMessages"));
        if (history_.size() >= historySize)
        {
            history_.remove(ServerConsts.FIRST_ELEM_INDEX);
            history_.add(message);
        }
        else
        {
            history_.add(message);
        }
    }
    private String getParticipantslist()
    {
        String result = new String();
        for(Map.Entry entry : connections_.entrySet())
        {
           result += ((ServerConnection)entry.getValue()).getName() + LINES_DELIMITER;
        }
        if(result.length() > 0)
        {
            result = result.substring(0, result.length() - LINES_DELIMITER.length());
        }
        return result;
    }
    int newSessionID()
    {
        Object[] keys = connections_.keySet().toArray();
        Arrays.sort(keys);
        if(keys.length == 0)
        {
            return 0;
        }
        int maxSessionID = (int)keys[keys.length - 1];
        return maxSessionID+1;
    }
}
