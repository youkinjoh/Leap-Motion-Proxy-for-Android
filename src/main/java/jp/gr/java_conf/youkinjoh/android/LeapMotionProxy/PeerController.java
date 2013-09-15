package jp.gr.java_conf.youkinjoh.android.LeapMotionProxy;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jp.gr.java_conf.youkinjoh.android.LeapMotionProxy.WebSocketListener.WebSocketEmitter;

import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.FrameConnection;


public class PeerController implements WebSocketEmitter {

	private OnClosePeerListener onClosePeerListener = null;
	private OnErrorListener onErrorListener = null;

	private WebSocketListener upSide = null;
	private WebSocketListener downSide = null;
	private FrameConnection upSideFrameConnection = null;
	private FrameConnection downSideFrameConnection = null;
	private Connection upSideConnection = null;
	private Connection downSideConnection = null;

	private Queue<StackMessage> queueMessageforUpSide = new ConcurrentLinkedQueue<StackMessage>();
	private Queue<StackMessage> queueMessageforDownSide = new ConcurrentLinkedQueue<StackMessage>();

	private boolean isCloseUpSide = false;
	private boolean isCloseDownSide = false;

	public PeerController() {
		upSide = new WebSocketListener(ConnectionType.UP_SIDE, this);
		downSide = new WebSocketListener(ConnectionType.DOWN_SIDE, this);
	}

	WebSocketListener getUpSide() {
		return upSide;
	}

	WebSocketListener getDownSide() {
		return downSide;
	}

	@Override
	public void onHandshake(ConnectionType type, FrameConnection connection) {
		switch (type) {
		case UP_SIDE:
			upSideFrameConnection = connection;
			break;
		case DOWN_SIDE:
			downSideFrameConnection = connection;
			break;
		default:
			break;
		}
	}

	@Override
	public void onOpen(ConnectionType type, Connection connection) {

		Connection conn = null;
		Queue<StackMessage> queue = null;
		switch (type) {
		case UP_SIDE:
			upSideConnection = connection;
			conn = connection;
			queue = queueMessageforUpSide;
			break;
		case DOWN_SIDE:
			downSideConnection = connection;
			conn = connection;
			queue = queueMessageforDownSide;
			break;
		default:
			break;
		}

		StackMessage message = null;
		while((message = queue.poll()) != null) {
			if (message.isText()) {
				TextData data = message.getTextData();
				try {
					conn.sendMessage(data.getData());
				} catch (IOException e) {
					sendError(e.getMessage());
				}
			}
			if (message.isByte()) {
				ByteData data = message.getByteData();
				try {
					conn.sendMessage(data.getData(), data.getOffSet(), data.getLength());
				} catch (IOException e) {
					sendError(e.getMessage());
				}
			}
		}

	}

	@Override
	public boolean onFrame(ConnectionType type, byte flags, byte opcode, byte[] data, int offset, int length) {
		return false;
	}

	@Override
	public boolean onControl(ConnectionType type, byte controlCode, byte[] data, int offset, int length) {
		return false;
	}

	@Override
	public void onClose(ConnectionType type, int closeCode, String message) {
		switch (type) {
		case UP_SIDE:
			if (downSideConnection != null) {
				downSideConnection.close(closeCode, message);
			}
			downSideConnection = null;
			downSideFrameConnection = null;
			isCloseDownSide = true;
			break;
		case DOWN_SIDE:
			if (upSideConnection != null) {
				upSideConnection.close(closeCode, message);
			}
			upSideConnection = null;
			upSideFrameConnection = null;
			isCloseUpSide = true;
			break;
		default:
			break;
		}
		if (isCloseUpSide && isCloseDownSide) {
			sendClose(null);
		}
	}

	@Override
	public void onMessage(ConnectionType type, String data) {

		Connection conn = null;
		Queue<StackMessage> queue = null;

		switch (type) {
		case UP_SIDE:
			conn  = downSideConnection;
			queue = queueMessageforDownSide;
			break;
		case DOWN_SIDE:
			conn  = upSideConnection;
			queue = queueMessageforUpSide;
			break;
		default:
			break;
		}

		if (conn != null && conn.isOpen()) {
			try {
				conn.sendMessage(data);
			} catch (IOException e) {
				sendError(e.getMessage());
			}
		} else {
			queue.add(new StackMessage(new TextData(data)));
		}

	}

	@Override
	public void onMessage(ConnectionType type, byte[] data, int offset, int length) {

		Connection conn = null;
		Queue<StackMessage> queue = null;

		switch (type) {
		case UP_SIDE:
			conn  = downSideConnection;
			queue = queueMessageforDownSide;
			break;
		case DOWN_SIDE:
			conn  = upSideConnection;
			queue = queueMessageforUpSide;
			break;
		default:
			break;
		}

		if (conn != null && conn.isOpen()) {
			try {
				conn.sendMessage(data, offset, length);
			} catch (IOException e) {
				sendError(e.getMessage());
			}
		} else {
			queue.add(new StackMessage(new ByteData(data, offset, length)));
		}

	}

	private void sendClose(String message) {
		if (onClosePeerListener == null) {
			return;
		}
		onClosePeerListener.onClosePeer(message);
	}

	private void sendError(String message) {
		if (onErrorListener == null) {
			return;
		}
		onErrorListener.onError(message);
	}

	public void setOnClosePeerListener(OnClosePeerListener onClosePeerListener) {
		this.onClosePeerListener = onClosePeerListener;
	}

	public void setOnErrorListener(OnErrorListener onErrorListener) {
		this.onErrorListener = onErrorListener;
	}

	public interface OnClosePeerListener {

		public void onClosePeer(String message);

	}

	public interface OnErrorListener {

		public void onError(String message);

	}

}
