package ch04;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.blob.BlobUploader;
import org.apache.activemq.command.ActiveMQQueue;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by zhixuan on 2017/6/2.
 */
public class QLender implements MessageListener{
    private QueueConnection connection = null;
    private QueueSession session = null;
    private Queue requestQueue = null;

    public QLender(String queuecf, String requestQueue){
        try {
            QueueConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            connection = factory.createQueueConnection();

            session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            this.requestQueue = new ActiveMQQueue(requestQueue);
            connection.start();

            QueueReceiver receiver = session.createReceiver(this.requestQueue);
            receiver.setMessageListener(this);

            System.out.println("Waiting for loan request...");
        } catch (JMSException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            boolean accepted = true;
            MapMessage mapMessage = (MapMessage) message;
            double salary = mapMessage.getDouble("Salary");
            double loanAmt = mapMessage.getDouble("LoanAmount");

            if (loanAmt < 200000){
                accepted = (salary / loanAmt) > 0.25;
            }else {
                accepted = (salary / loanAmt) > 0.33;
            }

            System.out.println("% = " + (salary / loanAmt) + ", loan is " + (accepted ? "Accepted!" : "Declined"));

            TextMessage textMessage = session.createTextMessage();
            textMessage.setText(accepted ? "Accepted!" : "Declined");
            textMessage.setJMSCorrelationID(message.getJMSMessageID());

            QueueSender sender = session.createSender((Queue) message.getJMSReplyTo());
            sender.send(message);

            System.out.println("\nWaiting for loan request...");

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
        System.exit(1);
    }

    public static void main(String[] args){
        String queueucf = args[0];
        String requestQueue = args[1];

        QLender lender = new QLender(queueucf, requestQueue);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("QLender application started...");
            System.out.println("Press enter to quit application...");
            String line = reader.readLine();
            lender.exit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
