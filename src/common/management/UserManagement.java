package common.management;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import common.user.User;
import server.ServerException;

public interface UserManagement extends Remote {
	User login(String username, String password) throws RemoteException, JoinException, ServerException;

	User register(String username, String password) throws RemoteException, JoinException, ServerException;

	void logout(User user) throws RemoteException, ServerException;

	ArrayList<User> getAllUsers() throws RemoteException, ServerException;

	ArrayList<User> getOnlineUsers() throws RemoteException, ServerException;
}
