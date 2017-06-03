package ch07;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

import javax.jms.*;

/**
 * Created by zhixuan on 2017/6/3.
 */
public class JMSSender {
    private QueueConnection connection = null;
    private QueueSession session = null;
    private QueueSender sender = null;

    public static void main(String[] args){
        JMSSender jmsSender = new JMSSender();
        jmsSender.sendMessageGroup();
        System.exit(0);
    }

    public JMSSender(){
        try {
            QueueConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            connection = factory.createQueueConnection();
            connection.start();
            session = connection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            Queue queue = new ActiveMQQueue("queue1");
            sender = session.createSender(queue);
        } catch (JMSException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    private void sendMessageGroup() {
        try {
            sendMessageMarker("START_SEQUENCE");
            sendMessage("First Message");
            sendMessage("Second Message");
            sendMessage("Third Message");
            sendMessageMarker("END_SEQUENCE");
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String text) throws JMSException {
        TextMessage message = session.createTextMessage(text);
        message.setStringProperty("JMSXGroupID", "GROUP1");
        sender.send(message);
    }

    private void sendMessageMarker(String text) throws JMSException {
        BytesMessage message = session.createBytesMessage();
        message.setStringProperty("JMSXGroupID", "GROUP1");
        message.setStringProperty("SequenceMarker", text);
        sender.send(message);
    }
}
