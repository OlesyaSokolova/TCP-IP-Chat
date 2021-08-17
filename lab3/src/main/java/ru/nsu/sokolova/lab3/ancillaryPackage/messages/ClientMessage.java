package ru.nsu.sokolova.lab3.ancillaryPackage.messages;

import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ClientMessagesDataTypes;

public class ClientMessage{
    public ClientMessage(ClientMessagesDataTypes type, String data)
    {
        type_ = type;
        data_ = data;
    }
    ClientMessagesDataTypes type_;
    String data_;

    public ClientMessagesDataTypes getType() {
        return type_;
    }

    public String getData() {
        return data_;
    }
}
