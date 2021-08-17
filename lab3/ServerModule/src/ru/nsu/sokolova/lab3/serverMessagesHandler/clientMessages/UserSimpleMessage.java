package ru.nsu.sokolova.lab3.serverMessagesHandler.clientMessages;

import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ClientMessagesDataTypes;
import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ClientXMLfields;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static ru.nsu.sokolova.lab3.ancillaryPackage.consts.MessagesConsts.*;
import static ru.nsu.sokolova.lab3.ConnectionConsts.EMPTY_STRING;

public class UserSimpleMessage extends ClientXMLmessage {
    @Override
    public String[] unpackMessage(NodeList nodeList) {
        String[] result  = new String[2];
        result[TYPE_INDEX] = EMPTY_STRING;
        result[DATA_INDEX] = EMPTY_STRING;
        result[TYPE_INDEX] = String.valueOf(ClientMessagesDataTypes.MESSAGE);
        Node msg = nodeList.item(0);
        if (msg.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) msg;
            result[DATA_INDEX] = getTagValue(ClientXMLfields.MESSAGE, element);
        }
        return result;
    }
}

