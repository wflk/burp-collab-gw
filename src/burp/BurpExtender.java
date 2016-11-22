package burp;

import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class BurpExtender extends Thread implements IBurpExtender, IExtensionStateListener
{
	private final static String NAME = "Collaborator gateway";
	private IBurpCollaboratorClientContext ccc;
	private final List<Closeable> sockets = new CopyOnWriteArrayList<Closeable>();

	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
	{
		callbacks.setExtensionName(NAME);
		callbacks.registerExtensionStateListener(this);
		ccc = callbacks.createBurpCollaboratorClientContext();
		start();
	}

	@Override
	public void extensionUnloaded() {
		for (Closeable s : sockets) try { s.close(); } catch (IOException ioe) {}
	}

	@Override
	public void run() {
		try {
			ServerSocket ss = new ServerSocket(8452);
			sockets.add(ss);
			ss.setReuseAddress(true);
			while (true) {
				Socket cs = ss.accept();
				sockets.add(cs);
				new ClientHandler(cs, ccc).start();

			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
