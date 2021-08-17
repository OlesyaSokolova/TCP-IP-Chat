package ru.nsu.sokolova.lab3.serverMessagesHandler.serverMessages;

import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ServerXMLfields;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

public class Error extends ServerXMLmessage {
    public Error() throws ParserConfigurationException
    {}

    @Override
    public Document packMessage(String data)
    {
        Document result = documentBuilder_.newDocument();
        Element rootElement = result.createElement(ServerXMLfields.ERROR);
        Node msg = createChild(result, ServerXMLfields.MESSAGE, data);
        rootElement.appendChild(msg);
        result.appendChild(rootElement);
        return result;
    }
}
