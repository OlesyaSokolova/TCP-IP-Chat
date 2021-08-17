package ru.nsu.sokolova.lab3.client;

import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ClientMessagesDataTypes;
import ru.nsu.sokolova.lab3.ancillaryPackage.consts.ServerMessagesDataTypes;
import ru.nsu.sokolova.lab3.ancillaryPackage.messages.ClientMessage;
import ru.nsu.sokolova.lab3.ancillaryPackage.messages.ServerMessage;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import static ru.nsu.sokolova.lab3.ancillaryPackage.consts.MessagesConsts.*;
import static ru.nsu.sokolova.lab3.ancillaryPackage.consts.SettingsConsts.*;
import static ru.nsu.sokolova.lab3.consts.ClientWindowConsts.*;
import static ru.nsu.sokolova.lab3.consts.ClientWindowConsts.EMPTY_STRING;
public class ClientWindow extends JFrame {

    private Properties settings_;
    private JTextArea chatField_;
    private JScrollPane chatScrollPane_;
    private JTextArea participantsList_;
    private JScrollPane participantsScrollPane_;
    private JTextField name_;
    private JTextField messageInput_;
    private ClientListener clientListener_;
    private ClientConnection connection_;
    private int serverPort_;
    private Color mainColor_;
    private Color bordersColor_;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new ClientWindow());
    }

    public class ClientListener {

        void acceptMessage(ServerMessage message)
        {
            SwingUtilities.invokeLater(() -> {
                if(message.getType().equals(ServerMessagesDataTypes.LISTUSERS))
                {
                    participantsList_.setText(SHOW_PARTICIPANTS);
                    String[] participants = message.getData().split(LINES_DELIMITER);
                    int i = 1;
                    for(String participant : participants)
                    {
                        participantsList_.append(i + "." + participant + LINES_DELIMITER);
                        i++;
                    }
                    participantsList_.setCaretPosition(participantsList_.getDocument().getLength());
                    if (participantsList_ != null)
                    {
                        participantsList_.revalidate();
                    }
                }
                else if(message.getType().equals(ServerMessagesDataTypes.NO_DATA_SUCCESS))
                {
                    return;
                }
                else if(message.getType().equals(ServerMessagesDataTypes.HISTORY))
                {
                    chatField_.append(SHOW_HISTORY);
                    chatField_.append(INFO_DELIMITER);
                    if( message.getData().equals(NO_DATA))
                    {
                        chatField_.append(NO_HISTORY);
                        chatField_.append(INFO_DELIMITER);
                        chatField_.setCaretPosition(chatField_.getDocument().getLength());
                        if (chatScrollPane_ != null) {
                            chatScrollPane_.revalidate();
                        }
                        return;
                    }
                    String[] events = message.getData().split(LINES_DELIMITER);
                    for(String event : events)
                    {
                        chatField_.append(event + LINES_DELIMITER);
                    }
                    chatField_.append(INFO_DELIMITER);
                    chatField_.setCaretPosition(chatField_.getDocument().getLength());
                    if (chatScrollPane_ != null) {
                        chatScrollPane_.revalidate();
                    }
                }
                else {
                    String data = message.getData();
                    chatField_.append(data + LINES_DELIMITER);
                    chatField_.setCaretPosition(chatField_.getDocument().getLength());
                    if (chatScrollPane_ != null) {
                        chatScrollPane_.revalidate();
                    }
                }
            });
        }

        public void printClientError(String error)
        {
            chatField_.append(INFO_DELIMITER);
            chatField_.append("ERROR WHLE SENDING THE MESSAGE" + SHOW_TEXT + LINES_DELIMITER);
            chatField_.append(error + LINES_DELIMITER);
            chatField_.append(INFO_DELIMITER);
            chatField_.setCaretPosition(chatField_.getDocument().getLength());
            if (chatScrollPane_ != null) {
                chatScrollPane_.revalidate();
            }
        }
    };

    private void loadSettings() {
        try {
            FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH);
            settings_ = new Properties();
            settings_.load(fis);
        } catch (IOException ex) {
            System.out.println(NO_CONFIG_FILE);
        }
    }


    void setConstraints(GridBagConstraints constraints) {
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridheight = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.ipadx = 0;
        constraints.ipady = 0;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
    }

    private void setNameThenStartChat() {
        setSize(SET_NAME_WIDTH, SET_NAME_HEIGHT);

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        setConstraints(constraints);

        JTextField suggestionToEnterName = new JTextField(SUGGESTION_TO_ENTER_NAME);
        suggestionToEnterName.setFont(new Font(FONT, Font.PLAIN, TEXT_SIZE));
        suggestionToEnterName.setEditable(false);
        gbl.setConstraints(suggestionToEnterName, constraints);
        suggestionToEnterName.setBorder(new LineBorder(bordersColor_, APP_TEXT_THICKNESS));

        suggestionToEnterName.setBackground(mainColor_);
        add(suggestionToEnterName, BorderLayout.NORTH);

        name_ = new JTextField(NAME_LINE_LENGTH);
        name_.setFont(new Font(FONT, Font.PLAIN, TEXT_SIZE));
        name_.setEditable(true);
        constraints.insets = new Insets(GAP_BEFORE_NAME, 0, 0, 0);
        constraints.gridy = GridBagConstraints.RELATIVE;
        gbl.setConstraints(name_, constraints);
        name_.setBorder(new LineBorder(bordersColor_, USER_TEXT_THICKNESS));
        name_.setBackground(USER_TEXT_FIELD_COLOR);
        add(name_);

        ActionListener listener = actionEvent -> {
            remove(suggestionToEnterName);
            remove(name_);
            gbl.setConstraints(name_, constraints);
            name_.setBackground(mainColor_);
            name_.setBorder(new LineBorder(bordersColor_, APP_TEXT_THICKNESS));
            name_.setEditable(false);
            setSize(CHAT_WIDTH, CHAT_HEIGHT);
            add(name_, BorderLayout.NORTH);
            serverPort_ = Integer.valueOf(settings_.getProperty("serverPort"));
            try {
                connection_ = new ClientConnection(clientListener_, LOCALHOST, serverPort_);
            }
            catch (IOException |CloneNotSupportedException e)
            {
                connectionRefused();
                return;
            }
            connection_.startConversation();
            setChatWindow();
            repaint();
        };
        name_.addActionListener(listener);
    }

    private void connectionRefused()
    {
        chatField_.setEditable(false);
        chatField_.setLineWrap(true);
        chatField_.setFont(new Font(FONT, Font.PLAIN, TEXT_SIZE));
        chatScrollPane_ = new JScrollPane(chatField_);
        add(chatScrollPane_, BorderLayout.CENTER);
        clientListener_.acceptMessage(new ServerMessage(ServerMessagesDataTypes.ERROR, CONNECTION_REFUSED));
    }

    private void setParticipantslist()
    {
        participantsList_.setEditable(false);
        participantsList_.setLineWrap(true);
        participantsList_.setFont(new Font(FONT, Font.PLAIN, TEXT_SIZE-3));
        participantsList_.setBackground(mainColor_);
        participantsScrollPane_ = new JScrollPane(participantsList_);
        participantsScrollPane_.setBorder(new LineBorder(bordersColor_, USER_TEXT_THICKNESS));
        participantsScrollPane_.setBackground(mainColor_);
        add(participantsScrollPane_, BorderLayout.EAST);
    }
    private void setChatWindow() {
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        chatField_.setEditable(false);
        chatField_.setLineWrap(true);
        setParticipantslist();

        connection_.sendMessage(new ClientMessage(ClientMessagesDataTypes.LOGIN, name_.getText()));
        ActionListener listener = actionEvent -> {
            String msg = messageInput_.getText();
            if (msg.equals(EMPTY_STRING)) {
                return;
            } else {
                messageInput_.setText(null);
                connection_.sendMessage(new ClientMessage(ClientMessagesDataTypes.MESSAGE, msg));
            }
        };
        messageInput_.addActionListener(listener);
        messageInput_.setFont(new Font(FONT, Font.PLAIN, TEXT_SIZE));
        chatField_.setFont(new Font(FONT, Font.PLAIN, TEXT_SIZE));
        chatScrollPane_ = new JScrollPane(chatField_);
        chatScrollPane_.setBorder(new LineBorder(bordersColor_, USER_TEXT_THICKNESS));
        add(chatScrollPane_, BorderLayout.CENTER);
        add(messageInput_, BorderLayout.SOUTH);
    }

    private ClientWindow() {
        super(CHAT_TITLE);
        loadSettings();
        messageInput_ = new JTextField();
        chatField_ = new JTextArea();
        participantsList_ = new JTextArea();
        clientListener_ = new ClientListener();
        mainColor_ = new Color(220, 182, 220);
        bordersColor_ = new Color(98, 49, 96);
        setNameThenStartChat();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(JOptionPane.showConfirmDialog(ClientWindow.this, CONFIRM_LEAVING_CHAT, LEAVING_CHAT, JOptionPane.YES_NO_OPTION ) == JOptionPane.OK_OPTION)
                {
                    setVisible(false);
                    if(connection_ != null)
                    {
                        connection_.sendMessage(new ClientMessage(ClientMessagesDataTypes.LOGOUT, name_.getText()));
                        connection_.disconnect();
                        ClientWindow.this.dispose();
                    }
                }
                else
                {
                    return;
                }
            }
        });
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setVisible(true);
    }
}
