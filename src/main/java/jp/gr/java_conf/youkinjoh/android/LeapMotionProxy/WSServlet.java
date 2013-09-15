package jp.gr.java_conf.youkinjoh.android.LeapMotionProxy;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.gr.java_conf.youkinjoh.android.LeapMotionProxy.PeerController.OnClosePeerListener;
import jp.gr.java_conf.youkinjoh.android.LeapMotionProxy.PeerController.OnErrorListener;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.eclipse.jetty.websocket.WebSocketServlet;


public class WSServlet extends WebSocketServlet {

	private URI forwardingUri = null;

	private OnConnectionListener onConnectionListener = null;

	private WebSocketClientFactory factory = new WebSocketClientFactory();
	private ConcurrentHashMap<String, PeerController> peers = new ConcurrentHashMap<String, PeerController>();

	private boolean isError = false;

	public WSServlet(String forwardingUrl) throws Exception {
		super();
		this.forwardingUri = new URI(forwardingUrl);
		factory.start();
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		final String peerId = UUID.randomUUID().toString();

		request.setAttribute("peerId", peerId);

		PeerController peer = new PeerController();
		peer.setOnClosePeerListener(new OnClosePeerListener() {
			@Override
			public void onClosePeer(String message) {
				peers.remove(peerId);
				sendMessage("close connection : peer id " + peerId);
			}
		});
		peer.setOnErrorListener(new OnErrorListener() {
			@Override
			public void onError(String message) {
				sendMessage(message + " : peer id " + peerId);
			}
		});

		peers.put(peerId, peer);

		WebSocketClient client = factory.newWebSocketClient();

		try {
			Future<WebSocket.Connection> future = client.open(forwardingUri, peer.getDownSide());
			WebSocket.Connection connection = future.get(10,TimeUnit.SECONDS);
		} catch (IOException e) {
			response.sendError(500, e.getMessage());
			isError = true;
			log(e.getMessage(), e);
			sendMessage("io error. : peer id " + peerId);
		} catch (InterruptedException e) {
			response.sendError(500, e.getMessage());
			isError = true;
			log(e.getMessage(), e);
			sendMessage("interrupted error. : peer id " + peerId);
		} catch (ExecutionException e) {
			response.sendError(500, e.getMessage());
			isError = true;
			log(e.getMessage(), e);
			sendMessage("execution error. : peer id " + peerId);
		} catch (TimeoutException e) {
			response.sendError(500, e.getMessage());
			isError = true;
			log(e.getMessage(), e);
			sendMessage("timeout error. : peer id " + peerId);
		}

		super.service(request, response);

	}

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		String peerId = (String)request.getAttribute("peerId");
		WebSocketListener upSide = peers.get(peerId).getUpSide();
		if (isError) {
			peers.remove(peerId);
		} else {
			sendMessage("open connection : peer id " + peerId);
		}
		return upSide;
	}

	private void sendMessage(String message) {
		if (onConnectionListener == null) {
			return;
		}
		onConnectionListener.onConnection(message);
	}

	public void setOnConnectionListener(OnConnectionListener onConnectionListener) {
		this.onConnectionListener  = onConnectionListener;
	}

	public interface OnConnectionListener {

		public void onConnection(String message);

	}

}
