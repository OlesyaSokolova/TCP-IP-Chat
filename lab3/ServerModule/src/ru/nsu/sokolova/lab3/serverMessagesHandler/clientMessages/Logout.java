package ru.nsu.sokolova.lab3.serverMessagesHandler.clientMessages;
import  ru.nsu.sokolova.lab3.ancillaryPackage.consts.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import static ru.nsu.sokolova.lab3.ancillaryPackage.consts.MessagesConsts.*;

public class Logout extends ClientXMLmessage {
    @Override
    public String[] unpackMessage(NodeList nodeList) {
        String[] result  = new String[2];
        result[TYPE_INDEX] = EMPTY_STRING;
        result[DATA_INDEX] = EMPTY_STRING;
        result[TYPE_INDEX] = String.valueOf(ClientMessagesDataTypes.LOGOUT);
        Node name = nodeList.item(0);
        if (name.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) name;
            result[DATA_INDEX] = getTagValue(ClientXMLfields.NAME, element);
        }
        return result;
    }
}
