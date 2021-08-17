package ru.nsu.sokolova.lab3.serverMessagesHandler.serverMessages;
import ru.nsu.sokolova.lab3.ConnectionConsts;
import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ServerXMLfields;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

public class Listusers extends ServerXMLmessage {
    public Listusers() throws ParserConfigurationException
    {}

    @Override
    public Document packMessage(String data)
    {
        Document result = documentBuilder_.newDocument();
        Element rootElement = result.createElement(ServerXMLfields.SUCCESS);
        Element listUsers = result.createElement(ServerXMLfields.LISTUSERS);
        String[] users = data.split(ConnectionConsts.LINES_DELIMITER);
        for(int i = 0 ; i < users.length; i++)
        {
            Node user_i = result.createElement(ServerXMLfields.USER);
            Node name = createChild(result, ServerXMLfields.NAME, users[i]);
            Node type = createChild(result, ServerXMLfields.TYPE, "CHAT_CLIENT_" + i + 1);
            user_i.appendChild(name);
            user_i.appendChild(type);
            listUsers.appendChild(user_i);
        }
        rootElement.appendChild(listUsers);
        result.appendChild(rootElement);
        return result;
    }
}
