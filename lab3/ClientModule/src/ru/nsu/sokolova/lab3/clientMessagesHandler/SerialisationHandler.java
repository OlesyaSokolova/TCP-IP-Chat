package ru.nsu.sokolova.lab3.clientMessagesHandler;

import ru.nsu.sokolova.lab3.ancillaryPackage.consts.MessagesHandlerTypes;
import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ServerMessagesDataTypes;
import ru.nsu.sokolova.lab3.ancillaryPackage.messages.ClientMessage;
import ru.nsu.sokolova.lab3.ancillaryPackage.messages.ServerMessage;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static ru.nsu.sokolova.lab3.ancillaryPackage.consts.MessagesConsts.*;

public class SerialisationHandler extends MessagesHandler {
    private ObjectInputStream socketInput_;
    private ObjectOutputStream socketOutput_;

    public SerialisationHandler() {
        super(MessagesHandlerTypes.SERIALISATION);
    }

    @Override
    public ServerMessage receiveMessage() throws Exception
    {
        String message = null;
        try
        {
            if(socketInput_ == null)
            {
                try {
                    socketInput_ = new ObjectInputStream(socket_.getInputStream());
                }
                catch(java.net.SocketTimeoutException ex)
                {
                    throw ex;
                }
            }
            if(!socket_.isClosed())
            {
               try {
                    message = (String) socketInput_.readObject();
                }
                catch (EOFException ex)
                {
                    throw ex;
                }
                catch(java.net.SocketTimeoutException ex)
                {
                    throw ex;
                }
            }
        }
        catch (java.net.SocketException ex)
        {
            throw ex;
        }
        catch(IOException  | ClassNotFoundException ex)
        {
            throw ex;
        }
        String[] messageContent = message.split(SERIALISED_DATA_DELIMITER);
        ServerMessagesDataTypes type = null;
        try {
            type = ServerMessagesDataTypes.valueOf(messageContent[TYPE_INDEX]);
        }
        catch(IllegalArgumentException ex)
        {
            throw ex;
        }
        String data = messageContent[DATA_INDEX];
        return new ServerMessage(type, data);
    }

    @Override
    public  void sendMessage(ClientMessage message) throws Exception
    {
        String messageString = packMessage(message);
        try {
            if (socketOutput_ == null)
            {
                socketOutput_ = new ObjectOutputStream(socket_.getOutputStream());
            }
            if (!socket_.isClosed()) {
                socketOutput_.writeObject(messageString);
                socketOutput_.flush();
            }
        }
        catch (IOException ex)
        {
            //throw new ChatClientException(ex.toString());
            throw ex;
        }
    }
    private String packMessage(ClientMessage message)
    {
        String result = message.getType().toString() + SERIALISED_DATA_DELIMITER + message.getData();
        return result;
    }
    @Override
    public void finishWork() {
        try {
            if(socketInput_!= null && socketOutput_ != null)
            {
                socketInput_.close();
                socketOutput_.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setSocket(Socket socket) {
        socket_ = socket;
    }
}
