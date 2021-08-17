package ru.nsu.sokolova.lab3.serverMessagesHandler.serverMessages;
import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ServerXMLfields;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

public class SessionID extends ServerXMLmessage {
    public SessionID() throws ParserConfigurationException
    {}

    @Override
    public Document packMessage(String data)
    {
        Document result = documentBuilder_.newDocument();
        Element rootElement = result.createElement(ServerXMLfields.SUCCESS);
        Node childElem = createChild(result, ServerXMLfields.SESSION, data);
        rootElement.appendChild(childElem);
        result.appendChild(rootElement);
        return result;
    }
}
