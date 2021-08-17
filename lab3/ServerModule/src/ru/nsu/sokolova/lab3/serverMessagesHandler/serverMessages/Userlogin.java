package ru.nsu.sokolova.lab3.serverMessagesHandler.serverMessages;

import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ServerXMLfields;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

public class Userlogin extends ServerXMLmessage
{
    public Userlogin() throws ParserConfigurationException
    {}

    @Override
    public Document packMessage(String data)
    {
        Document result = documentBuilder_.newDocument();
        Element rootElement = result.createElement(ServerXMLfields.EVENT);
        rootElement.setAttribute(ServerXMLfields.EVENT, ServerXMLfields.USERLOGIN);
        Node name = createChild(result, ServerXMLfields.NAME, data);
        rootElement.appendChild(name);
        result.appendChild(rootElement);
        return result;
    }
}
