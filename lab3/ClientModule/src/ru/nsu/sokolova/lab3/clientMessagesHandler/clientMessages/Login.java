package ru.nsu.sokolova.lab3.clientMessagesHandler.clientMessages;

import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ClientXMLfields;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

public class Login extends ClientXMLmessage {
    public Login() throws ParserConfigurationException {
    }

    @Override
    public Document packMessage(String data) {
        Document result = documentBuilder_.newDocument();
        Element rootElement = result.createElement(ClientXMLfields.COMMAND);
        rootElement.setAttribute(ClientXMLfields.COMMAND, ClientXMLfields.LOGIN);
        Node name = createChild(result, ClientXMLfields.NAME, data);
        rootElement.appendChild(name);
        Node type = createChild(result, ClientXMLfields.TYPE, "CHAT_CLIENT_NAME");
        rootElement.appendChild(type);
        result.appendChild(rootElement);
        return result;
    }
}
