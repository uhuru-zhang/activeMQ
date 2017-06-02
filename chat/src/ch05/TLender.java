package ch05;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.util.StringUtils;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by zhixuan on 2017/6/2.
 */
public class TLender {
    private TopicConnection connection = null;
    private TopicSession session = null;
    private Topic topic = null;

    private TLender(String topiccf, String topicName){
        try {
            TopicConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            TopicConnection connection = factory.createTopicConnection();
            session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            topic = new ActiveMQTopic(topicName);
            connection.start();
        } catch (JMSException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void publishRate(double newRate){
        try {
            BytesMessage bytesMessage = session.createBytesMessage();
            bytesMessage.writeDouble(newRate);

            TopicPublisher publisher = session.createPublisher(topic);
            publisher.setDeliveryMode(DeliveryMode.PERSISTENT);// 设置持久化传输

            publisher.publish(bytesMessage);
        } catch (JMSException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void exit(){
        try{
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    public static void main(String[] args){
        String topiccf = args[0];
        String topicName = args[1];

        TLender lender = new TLender(topiccf, topicName);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("TLender Application started...");
            System.out.println("Press enter  to quit application...");
            System.out.println("Enter: Rate");
            System.out.println("e.g. 6.8");

            while (true){
                System.out.print("> ");
                String rateString = reader.readLine();
                if (StringUtils.isEmpty(rateString.trim()))
                    lender.exit();
                double rate = Double.parseDouble(rateString.trim());
                lender.publishRate(rate);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
