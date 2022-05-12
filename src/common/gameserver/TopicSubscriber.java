package common.gameserver;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class TopicSubscriber {

	private String host;
	private String connectionFactoryAddr;
	private String topicAddr;

	public TopicSubscriber(String host, String connectionFactoryAddr, String topicAddr)
			throws NamingException, JMSException {
		this.host = host;
		this.connectionFactoryAddr = connectionFactoryAddr;
		this.topicAddr = topicAddr;

		// Access JNDI
		createJNDIContext();

		// Lookup JMS resources
		lookupConnectionFactory();
		lookupTopic();

		// Create connection->session->sender
		createConnection();

		createSession();
		createReceiver();

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

	private TopicConnectionFactory topicConnectionFactory;

	private void lookupConnectionFactory() throws NamingException {

		try {
			topicConnectionFactory = (TopicConnectionFactory) jndiContext.lookup(connectionFactoryAddr);
		} catch (NamingException e) {
			System.err.println("JNDI API JMS connection factory lookup failed: " + e);
			throw e;
		}
	}

	private Topic topic;

	private void lookupTopic() throws NamingException {

		try {
			topic = (Topic) jndiContext.lookup(topicAddr);
		} catch (NamingException e) {
			System.err.println("JNDI API JMS topic lookup failed: " + e);
			throw e;
		}
	}

	private TopicConnection connection;

	private void createConnection() throws JMSException {
		try {
			connection = topicConnectionFactory.createTopicConnection();
			connection.start();
		} catch (JMSException e) {
			System.err.println("Failed to create connection to JMS provider: " + e);
			throw e;
		}
	}

	private TopicSession session;

	private void createSession() throws JMSException {
		try {
			session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}

	// Was: QueueSender
	private javax.jms.TopicSubscriber topicSubscriber;

	private void createReceiver() throws JMSException {
		try {
			topicSubscriber = session.createSubscriber(topic);
		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}

	public void receiveMessage() throws JMSException {

		Message m = topicSubscriber.receive();
		if (m != null && m instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) m;
			System.out.println("Received message: " + textMessage.getText());
		}
	}

	public Game receiveGame() throws JMSException {

		Message m = topicSubscriber.receive();
		if (m != null && m instanceof ObjectMessage) {
			ObjectMessage objMessage = (ObjectMessage) m;
			System.out.println("Received game");
			return (Game) objMessage.getObject();
		}

		return null;
	}

	public Answer receiveAnswer() throws JMSException {

		Message m = topicSubscriber.receive();
		if (m != null && m instanceof ObjectMessage) {
			ObjectMessage objMessage = (ObjectMessage) m;
			System.out.println("Received answer");
			return (Answer) objMessage.getObject();
		}

		return null;
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
