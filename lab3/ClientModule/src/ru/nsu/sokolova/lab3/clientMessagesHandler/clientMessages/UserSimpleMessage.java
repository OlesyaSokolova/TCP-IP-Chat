package ru.nsu.sokolova.lab3.clientMessagesHandler.clientMessages;

import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ClientXMLfields;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

public class UserSimpleMessage extends ClientXMLmessage {
    public UserSimpleMessage() throws ParserConfigurationException {
    }
    @Override
    public Document packMessage(String data) {
        Document result = documentBuilder_.newDocument();
        Element rootElement = result.createElement(ClientXMLfields.COMMAND);
        rootElement.setAttribute(ClientXMLfields.COMMAND, ClientXMLfields.MESSAGE);
        Node msg = createChild(result, ClientXMLfields.MESSAGE, data);
        rootElement.appendChild(msg);
        result.appendChild(rootElement);
        return result;
    }
}

