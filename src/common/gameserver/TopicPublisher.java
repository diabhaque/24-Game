package common.gameserver;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
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

public class TopicPublisher {

	private String host;
	private String connectionFactoryAddr;
	private String topicAddr;

	public TopicPublisher(String host, String connectionFactoryAddr, String topicAddr)
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
	private javax.jms.TopicPublisher topicPublisher;

	private void createSender() throws JMSException {
		try {
			topicPublisher = session.createPublisher(topic);
			topicPublisher.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}

	public void sendMessage(String text) throws JMSException {
		TextMessage message = session.createTextMessage();
		message.setText(text);
		topicPublisher.publish(message);
		System.out.println("Sending message " + text);
		// topicPublisher.send(session.createMessage());
	}

	public void sendGame(Game game) throws JMSException {
		ObjectMessage message = session.createObjectMessage(game);
		topicPublisher.publish(message);
		System.out.println("Sending game");
		// topicPublisher.send(session.createMessage());
	}

	public void sendAnswer(Answer answer) throws JMSException {
		ObjectMessage message = session.createObjectMessage(answer);
		topicPublisher.publish(message);
		System.out.println("Sending answer");
		// topicPublisher.send(session.createMessage());
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
