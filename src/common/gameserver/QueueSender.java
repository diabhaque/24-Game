package common.gameserver;

import java.util.Random;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class QueueSender {

	private String host;
	private String connectionFactoryAddr;
	private String queueAddr;

	public QueueSender(String host, String connectionFactoryAddr, String queueAddr)
			throws NamingException, JMSException {
		this.host = host;
		this.connectionFactoryAddr = connectionFactoryAddr;
		this.queueAddr = queueAddr;

		// Access JNDI
		createJNDIContext();

		// Lookup JMS resources
		lookupConnectionFactory();
		lookupQueue();

		// Create connection->session->sender
		createConnection();

		createSession();
		createSender();

	}

	private Context jndiContext;

	private void createJNDIContext() throws NamingException {
		System.setProperty("org.omg.CORBA.ORBInitialHost", host);
		System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
		try {
			jndiContext = new InitialContext();
		} catch (NamingException e) {
			System.err.println("Could not create JNDI API context: " + e);
			throw e;
		}
	}

	private ConnectionFactory connectionFactory;

	private void lookupConnectionFactory() throws NamingException {

		try {
			connectionFactory = (ConnectionFactory) jndiContext.lookup(connectionFactoryAddr);
		} catch (NamingException e) {
			System.err.println("JNDI API JMS connection factory lookup failed: " + e);
			throw e;
		}
	}

	private Queue queue;

	private void lookupQueue() throws NamingException {

		try {
			queue = (Queue) jndiContext.lookup(queueAddr);
		} catch (NamingException e) {
			System.err.println("JNDI API JMS queue lookup failed: " + e);
			throw e;
		}
	}

	private Connection connection;

	private void createConnection() throws JMSException {
		try {
			connection = connectionFactory.createConnection();
			connection.start();
		} catch (JMSException e) {
			System.err.println("Failed to create connection to JMS provider: " + e);
			throw e;
		}
	}

	public void sendMessage(String text) throws JMSException {
		TextMessage message = session.createTextMessage();
		message.setText(text);
		queueSender.send(message);
		System.out.println("Sending message " + text);
		// queueSender.send(session.createMessage());
	}

	public void sendAnswer(Answer answer) throws JMSException {
		ObjectMessage message = session.createObjectMessage(answer);
		queueSender.send(message);
		System.out.println("Sending answer");
		// queueSender.send(session.createMessage());
	}

	private Session session;

	private void createSession() throws JMSException {
		try {
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}

	// Was: QueueSender
	private MessageProducer queueSender;

	private void createSender() throws JMSException {
		try {
			queueSender = session.createProducer(queue);
		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}

	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (JMSException e) {
			}
		}
	}

}
