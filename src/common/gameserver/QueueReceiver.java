package common.gameserver;

import java.util.ArrayList;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import common.management.JoinException;
import common.user.User;
import server.ServerException;

public class QueueReceiver {

	private String host;
	private String connectionFactoryAddr;
	private String queueAddr;

	public QueueReceiver(String host, String connectionFactoryAddr, String queueAddr)
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

	public Game waitForGame(java.sql.Connection conn) throws JMSException, ServerException {

		ArrayList<User> players = new ArrayList<User>();

		// 1 player set start_time
		// 2 player set rem_time = 10 - (current_time - start_time)
		// if rem_time <= 0: start game
		// else queueReceiver.receive(timeout = rem_time);

		Message m;
		long startTime = 0l;
		long remainingTime = 0l;
		boolean startTimeout = false;

		while (players.size() < 4) {
			// need to add the 10 second wait thing
			if (startTimeout) {
				m = queueReceiver.receive(remainingTime);
				if (m == null) {
					return new Game(players);
				}
			} else {
				m = queueReceiver.receive();
			}

			if (m != null && m instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) m;
				String username = textMessage.getText();
				System.out.println("Received request from " + username);

				if (!User.isUserOnline(username, conn)) {
					continue;
				}

				players.add(User.getUser(username, "", conn));
				System.out.println("Received " + (players.size()) + " players");

				if (players.size() == 1) {
					startTime = System.currentTimeMillis();
				}

				if (players.size() == 2) {
					remainingTime = 10 * 1000l - (System.currentTimeMillis() - startTime);
					if (remainingTime <= 0) {
						break;
					} else {
						startTimeout = true;
					}
				}
			}
		}

		return new Game(players);
	}

	public Answer receiveAnswer() throws JMSException {

		Message m = queueReceiver.receive();
		if (m != null && m instanceof ObjectMessage) {
			ObjectMessage objMessage = (ObjectMessage) m;
			System.out.println("Received answer");
			return (Answer) objMessage.getObject();
		}

		return null;
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

	// Was: QueueReceiver
	private MessageConsumer queueReceiver;

	private void createReceiver() throws JMSException {
		try {
			queueReceiver = session.createConsumer(queue);
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
