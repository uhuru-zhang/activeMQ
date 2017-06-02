package ch02;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;

import javax.jms.*;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by zhixuan on 2017/5/31.
 */
public class Chat implements MessageListener{
    private TopicSession publisherSession;
    private TopicPublisher topicPublisher;
    private TopicConnection topicConnection;
    private String username;

    public Chat(String topicFactory, String topicName, String username) throws NamingException, JMSException {
        TopicConnectionFactory topicConnectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        TopicConnection topicConnection = topicConnectionFactory.createTopicConnection();

        TopicSession publisherSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        TopicSession subscriberSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

        Topic chatTopic = new ActiveMQTopic("topic1");

        TopicPublisher topicPublisher = publisherSession.createPublisher(chatTopic);
        TopicSubscriber topicSubscriber = subscriberSession.createSubscriber(chatTopic, null, true);

        topicSubscriber.setMessageListener(this);

        this.topicConnection = topicConnection;
        this.topicPublisher = topicPublisher;
        this.publisherSession = publisherSession;
        this.username = username;

        topicConnection.start();

    }
    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            System.out.println(textMessage.getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public void close() throws JMSException {
        topicConnection.close();
    }

    public void writeMessage(String message) throws JMSException {
        TextMessage textMessage = publisherSession.createTextMessage();
        textMessage.setText(username + ": " + message);
        topicPublisher.publish(textMessage);
    }
    public static void main(String[] args){
        try {
            if (args.length != 3){
                System.out.println("Factory, Topic, or username missing");
            }

            Chat chat = new Chat(args[0], args[1], args[2]);
            BufferedReader commandLine = new BufferedReader(new InputStreamReader(System.in));

            while (true){
                String line = commandLine.readLine();
                if (line.equalsIgnoreCase("exit")){
                    chat.close();
                    System.exit(0);
                }else {
                    chat.writeMessage(line);
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
