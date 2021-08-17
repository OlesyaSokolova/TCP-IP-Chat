package ru.nsu.sokolova.lab3.serverMessagesHandler.serverMessages;

import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ServerXMLfields;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

import static ru.nsu.sokolova.lab3.ConnectionConsts.LINES_DELIMITER;

public class History extends ServerXMLmessage
{
    public History() throws ParserConfigurationException
    {}

    @Override
    public Document packMessage(String data)
    {
        Document result = documentBuilder_.newDocument();
        Element rootElement =  result.createElement(ServerXMLfields.SUCCESS);
        Element listEvents = result.createElement(ServerXMLfields.HISTORY);
        String[] events = data.split(LINES_DELIMITER);
        for(int i = 0 ; i < events.length; i++)
        {
            Node event = createChild(result, ServerXMLfields.EVENT, events[i]);
            listEvents.appendChild(event);
        }
        rootElement.appendChild(listEvents);
        result.appendChild(rootElement);
        return result;
    }
}
