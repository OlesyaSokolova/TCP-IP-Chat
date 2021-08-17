package ru.nsu.sokolova.lab3.clientMessagesHandler;
import ru.nsu.sokolova.lab3.ancillaryPackage.consts.*;
import ru.nsu.sokolova.lab3.ancillaryPackage.messages.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import ru.nsu.sokolova.lab3.clientMessagesHandler.clientMessages.*;
import ru.nsu.sokolova.lab3.consts.ClientWindowConsts;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;

import static ru.nsu.sokolova.lab3.ancillaryPackage.consts.MessagesConsts.*;

public class XMLhandler extends MessagesHandler
{
    private BufferedReader socketInput_;
    private BufferedWriter socketOutput_;
    DocumentBuilder documentBuilder_;
    HashMap<ClientMessagesDataTypes, ClientXMLmessage> clientMessages_;
    public XMLhandler()
    {
        super(MessagesHandlerTypes.XML);
        clientMessages_  =new HashMap<>();
        try {
            clientMessages_.put(ClientMessagesDataTypes.LOGIN, new Login());
            clientMessages_.put(ClientMessagesDataTypes.MESSAGE, new UserSimpleMessage());
            clientMessages_.put(ClientMessagesDataTypes.LOGOUT, new Logout());

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    };

    @Override
    public void setSocket(Socket socket) {
        socket_ = socket;
    }

    @Override
    public ServerMessage receiveMessage() throws Exception
    {
        String message = null;
        try
        {
            if(socketInput_ == null)
            {
                try {
                    socketInput_ = new BufferedReader(new InputStreamReader(socket_.getInputStream()));
                }
                catch(java.net.SocketTimeoutException ex)
                {
                    throw ex;
                }
            }
            if(!socket_.isClosed())
            {
                try {
                    message = socketInput_.readLine();
                }
                catch (EOFException ex)
                {
                    throw ex;
                }
                catch(java.net.SocketTimeoutException ex)
                {
                    throw ex;
                }
            }
        }
        catch (java.net.SocketException ex)
        {
            throw ex;
        }
        catch(IOException ex)
        {
            throw ex;
        }
        String[] messageContent=null;
        try {
            messageContent = unpackMessage(message);
        }
        catch (NullPointerException ex)
        {
           throw ex;
        }
        catch (Exception ex)
        {
            throw ex;
        }
        ServerMessagesDataTypes type = null;
        try {
            type = ServerMessagesDataTypes.valueOf(messageContent[TYPE_INDEX]);
        }
        catch(IllegalArgumentException ex)
        {
            throw ex;
        }
        String data = messageContent[DATA_INDEX];
        return new ServerMessage(type, data);
    }
    @Override
    public void sendMessage(ClientMessage message) throws Exception
    {
        Document messageDocument = packMessage(message);
        String messageString = documentToString(messageDocument);
        try {
            if(socketOutput_ == null)
            {
                socketOutput_ = new BufferedWriter( new OutputStreamWriter(socket_.getOutputStream()));
            }
            if(!socket_.isClosed())
            {
                socketOutput_.write(messageString + ClientWindowConsts.LINES_DELIMITER);
                socketOutput_.flush();
            }
        } catch (IOException ex)
        {
            throw ex;
        }
    }

    private Document packMessage(ClientMessage message)
    {
        Document document = clientMessages_.get(message.getType()).packMessage(message.getData());
        return document;
    }

    private String[] unpackMessage(String message) throws Exception
    {
        Document document = null;
        try {
                document = stringToDocument(message);
        } catch (Exception ex)
        {
            throw ex;
        }
        document.getDocumentElement().normalize();
        Element rootElement = document.getDocumentElement();
        String nodeName = rootElement.getNodeName();
        String attributes = null;
        NodeList nodeList = document.getElementsByTagName(rootElement.getNodeName());
        String[] result  = new String[2];
        result[TYPE_INDEX] = ClientWindowConsts.EMPTY_STRING;
        result[DATA_INDEX] = ClientWindowConsts.EMPTY_STRING;
        if(nodeName.equals(ServerXMLfields.EVENT))
        {
            attributes = rootElement.getAttribute(nodeName);
            if(attributes.equals(ServerXMLfields.MESSAGE)) {
                result[TYPE_INDEX] = String.valueOf(ServerMessagesDataTypes.MESSAGE);
                Node firstNode = nodeList.item(0);
                if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) firstNode;
                    result[DATA_INDEX] = getTagValue(ServerXMLfields.MESSAGE, element);
                }
            }
            else if(attributes.equals(ServerXMLfields.USERLOGIN)) {
                result[TYPE_INDEX] =  String.valueOf(ServerMessagesDataTypes.USERLOGIN);
                Node firstNode = nodeList.item(0);
                if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) firstNode;
                    result[DATA_INDEX] = getTagValue(ServerXMLfields.NAME, element);
                }
            }
            else if(attributes.equals(ServerXMLfields.USERLOGOUT)) {
                result[TYPE_INDEX] =  String.valueOf(ServerMessagesDataTypes.USERLOGOUT);
                Node firstNode = nodeList.item(0);
                if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) firstNode;
                    result[DATA_INDEX] = getTagValue(ServerXMLfields.NAME, element);
                }
            }
        }
        else {
            if(nodeName.equals(ServerXMLfields.SUCCESS))
            {
                Node firstNode = nodeList.item(0);
                if (!firstNode.hasChildNodes())
                {
                    result[TYPE_INDEX] = String.valueOf(ServerMessagesDataTypes.NO_DATA_SUCCESS);
                    result[DATA_INDEX] = NO_DATA;
                    return result;
                }
                Node first = firstNode.getFirstChild();
                if (first.getNodeType() == Node.ELEMENT_NODE)
                {
                    String firstChildName = first.getNodeName();
                    if (firstChildName.equals(ServerXMLfields.SESSION))
                    {
                        Element element = (Element)firstNode;
                        result[TYPE_INDEX] =  String.valueOf(ServerMessagesDataTypes.SESSION_ID);
                        result[DATA_INDEX] = getTagValue(firstChildName, element);
                    }
                    else if (firstChildName.equals(ServerXMLfields.LISTUSERS))
                    {
                        result[TYPE_INDEX] =  String.valueOf(ServerMessagesDataTypes.LISTUSERS);
                        NodeList childList = document.getElementsByTagName(firstNode.getNodeName());
                        NodeList users = childList.item(0).getChildNodes().item(0).getChildNodes();
                        for(int i = 0; i < users.getLength(); i++)
                        {
                            result[DATA_INDEX] += (getTagValue(ServerXMLfields.NAME, (Element) users.item(i)) + ClientWindowConsts.LINES_DELIMITER);
                        }
                    }
                    else if (firstChildName.equals(ServerXMLfields.HISTORY))
                    {
                        NodeList childList = document.getElementsByTagName(firstNode.getNodeName());
                        NodeList events = childList.item(0).getChildNodes().item(0).getChildNodes();
                        result[TYPE_INDEX] =  String.valueOf(ServerMessagesDataTypes.HISTORY);
                        for (int i = 0; i < events.getLength() - 1 ; i++)
                        {
                            result[DATA_INDEX] += (events.item(i).getTextContent() +  ClientWindowConsts.LINES_DELIMITER);
                        }
                        result[DATA_INDEX] += events.item(events.getLength() - 1).getTextContent();
                    }
                }
            }
            else if(nodeName.equals(ServerXMLfields.ERROR))
            {
                result[TYPE_INDEX] =  String.valueOf(ServerMessagesDataTypes.ERROR);
                Node name = nodeList.item(0);
                if (name.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element element = (Element) name;
                    result[DATA_INDEX] = getTagValue(ServerXMLfields.MESSAGE, element);
                }
            }
        }
        return result;
    }
    private Document stringToDocument(String xmlString) throws Exception
    {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        Document result = null;
        try {
            docBuilder = builderFactory.newDocumentBuilder();
            result  = docBuilder.parse(new InputSource(new StringReader(xmlString)));
        }
        catch(Exception ex)
        {
            throw ex;
        }
        return result;
    }
    private  String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }
    private String documentToString(Document doc)
    {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transfObject;
        String result = null;
        try
        {
            transfObject = tFactory.newTransformer();
            transfObject.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transfObject.transform(new DOMSource(doc), new StreamResult(writer));
            result =  writer.getBuffer().toString();
        }
        catch (TransformerException e)
        {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public void finishWork() {
        try {
            if(socketInput_!= null && socketOutput_ != null)
            {
                socketInput_.close();
                socketOutput_.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
