package ch04;

import com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.util.StringUtils;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 * Created by zhixuan on 2017/6/1.
 */

public class QBorrower {
    private QueueConnection queueConnection = null;
    private QueueSession queueSession = null;
    private Queue responseQueue = null;
    private Queue requestQueue = null;

    public QBorrower(String queuecf, String requsetQueue, String responseQueue){
        try {
            QueueConnectionFactory factory = new ActiveMQConnectionFactory();
            QueueConnection connection = factory.createQueueConnection();

            queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            this.requestQueue = new ActiveMQQueue(requsetQueue);
            this.responseQueue = new ActiveMQQueue(responseQueue);

            connection.start();
        } catch (JMSException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void sendLoanRequest(double salary, double loanAmt){
        try {
            MapMessage mapMessage = queueSession.createMapMessage();
            mapMessage.setDouble("Salary", salary);
            mapMessage.setDouble("LoanAmt", loanAmt);
            mapMessage.setJMSReplyTo(responseQueue);

            QueueSender sender = queueSession.createSender(requestQueue);
            sender.send(mapMessage);

            String filter = "JMSCorrelationID = '" + mapMessage.getJMSMessageID() + "'";
            QueueReceiver receiver = queueSession.createReceiver(responseQueue, filter);
            TextMessage textMessage = (TextMessage) receiver.receive(30000);
            if (textMessage == null){
                System.out.println("QLender not responding!!!");
            }else {
                System.out.println("Loan request was " + textMessage.getText());
            }

        } catch (JMSException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void exit(){
        try {
            queueConnection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        String queuecf = args[0];
        String requestQueue = args[1];
        String responseQueue = args[2];
        QBorrower borrower = new QBorrower(queuecf, requestQueue, responseQueue);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("QBorrower Application started");
        System.out.println("Press enter to quit application");
        System.out.println("Enter: Salary, Loan_AMount");
        System.out.println("\ne.g. 50000, 120000");

        while (true){
            System.out.print(">");
            String loanrequest = reader.readLine();
            if (StringUtils.isEmpty(loanrequest.trim())){
                borrower.exit();
            }

            StringTokenizer tokenizer = new StringTokenizer(loanrequest, ",");
            double salary = Double.parseDouble(tokenizer.nextToken().trim());
            double loanAmt = Double.parseDouble(tokenizer.nextToken().trim());
        }
    }
}
