package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.client.ClientGuiModel;
import com.javarush.task.task30.task3008.client.ClientGuiView;
import java.net.*;

public class ClientGuiController extends Client{
	private ClientGuiModel model = new ClientGuiModel();
	private ClientGuiView view = new ClientGuiView(this);

	public class GuiSocketThread extends SocketThread{
		@Override
		protected void processIncomingMessage(String message){
			model.setNewMessage(message);
			view.refreshMessages();
		}

		@Override
		protected void informAboutAddingNewUser(String userName){
			model.addUser(userName);
			view.refreshUsers();
		}

		@Override
		protected void informAboutDeletingNewUser(String userName){
			model.deleteUser(userName);
			view.refreshUsers();
		}

		@Override
		protected void notifyConnectionStatusChanged(boolean clientConnected){
			super.notifyConnectionStatusChanged(clientConnected);
			view.notifyConnectionStatusChanged(clientConnected);
		}
	}

	@Override
	protected String getServerAddress(){
		return view.getServerAddress();
	}

	@Override
	protected int getServerPort(){
		return view.getServerPort();
	}

	@Override
	protected String getUserName(){
		return view.getUserName();
	}

	@Override
	protected SocketThread getSocketThread(){
		return new GuiSocketThread();
	}

	@Override
	public void run(){
		SocketThread socketThread = getSocketThread();
		socketThread.run();
	}

	public ClientGuiModel getModel () {
		return model;
	}

	public static void main(String[] args){
		ClientGuiController guiController = new ClientGuiController();
		guiController.run();
	}
}
