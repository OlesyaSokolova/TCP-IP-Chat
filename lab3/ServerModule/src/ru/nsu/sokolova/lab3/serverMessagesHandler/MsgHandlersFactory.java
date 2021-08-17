package ru.nsu.sokolova.lab3.serverMessagesHandler;

import ru.nsu.sokolova.lab3.ancillaryPackage.consts.MessagesHandlerTypes;

import java.util.HashMap;
import java.util.Map;

public class MsgHandlersFactory
{
    private Map<MessagesHandlerTypes, MessagesHandler> handlersMap_;

    public MsgHandlersFactory()
    {
        handlersMap_ = new HashMap<MessagesHandlerTypes, MessagesHandler>();
        handlersMap_.put(MessagesHandlerTypes.SERIALISATION, new SerialisationHandler());
        handlersMap_.put(MessagesHandlerTypes.XML, new XMLhandler());
    }

    public  MessagesHandler createHandler(MessagesHandlerTypes type) throws CloneNotSupportedException
    {
        MessagesHandler handler = null;
        handler = handlersMap_.get(type);
        MessagesHandler clone = null;
        clone = handler.clone();
        return clone;
    }
}
