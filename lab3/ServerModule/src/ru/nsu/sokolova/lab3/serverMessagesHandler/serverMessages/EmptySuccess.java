package ru.nsu.sokolova.lab3.serverMessagesHandler.serverMessages;

import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ServerXMLfields;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;

public class EmptySuccess extends ServerXMLmessage {
    public EmptySuccess() throws ParserConfigurationException
    {}

    @Override
    public Document packMessage(String data)
    {
        Document result = documentBuilder_.newDocument();
        Element rootElement = result.createElement(ServerXMLfields.SUCCESS);
        result.appendChild(rootElement);
        return result;
    }
}
