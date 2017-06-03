package ch07;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhixuan on 2017/6/3.
 */
public class JMSReceiver implements MessageListener{
    private List<String> messageBuffer = new ArrayList<>();

    public JMSReceiver(){
        try {
            QueueConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            QueueConnection connection = factory.createQueueConnection();
            connection.start();

            QueueSession session = connection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            Queue queue = session.createQueue("queue1");

            QueueReceiver receiver = session.createReceiver(queue);
            receiver.setMessageListener(this);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message.propertyExists("SequenceMarker")){
                String marker = message.getStringProperty("SequenceMarker");

                if (marker.equalsIgnoreCase("START_SEQUENCE")){
                    if (message.getJMSRedelivered())
                        processCompensatingTransaction();
                    messageBuffer.clear();
                }

                if (marker.equalsIgnoreCase("END_SEQUENCE")){
                    System.out.println("Messages: ");
                    messageBuffer.forEach(System.out::println);
                    message.acknowledge();
                }
            }

            if (message instanceof TextMessage){
                TextMessage textMessage = (TextMessage) message;
                processInterimMessage(textMessage.getText());
            }
            System.out.println("Waiting for next message!");
        } catch (JMSException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void processInterimMessage(String text) {
        messageBuffer.add(text);
    }

    private void processCompensatingTransaction() {
        messageBuffer.clear();
    }

    public static void main(String[] args){
        new JMSReceiver();
    }
}
