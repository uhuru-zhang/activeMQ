package ch05;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.console.command.store.proto.MapEntryPB;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * Created by zhixuan on 2017/6/2.
 */
public class TBorrower implements MessageListener {
    private TopicConnection connection = null;
    private TopicSession session = null;
    private Topic topic = null;

    private double currnetRate;

    public TBorrower(String topiccf, String topicName, String rate) {
        try {
            currnetRate = Double.parseDouble(rate);

            TopicConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            connection = factory.createTopicConnection();
            connection.setClientID("1");
            session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            topic = new ActiveMQTopic(topicName);

            // 非持久 TopicSubscriber subscriber = session.createSubscriber(topic);
            TopicSubscriber subscriber = session.createDurableSubscriber(topic, "b1");//持久
            subscriber.setMessageListener(this);

            connection.start();
            System.out.println("Waiting  for loan request...");
        } catch (JMSException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    @Override
    public void onMessage(Message message) {
        try {
            BytesMessage bytesMessage = (BytesMessage) message;
            double newRate = bytesMessage.readDouble();

            if ((currnetRate - newRate) >= 1.0) {
                System.out.println("New rate = " + newRate + " - Consider refinancing loan...");
            } else {
                System.out.println("New Rate = " + newRate + " - Keep existing loan...");
            }

            System.out.println();
            System.out.println("Waiting for rate update...");
        } catch (JMSException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void exit(){
        try {
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void main(String[] args){
        String topiccf = args[0];
        String topicName = args[1];
        String rate = args[2];

        TBorrower borrower = new TBorrower(topiccf, topicName, rate);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("TBorrower application started...");
            System.out.println("Press enter to exit...");
            reader.readLine();
            borrower.exit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
