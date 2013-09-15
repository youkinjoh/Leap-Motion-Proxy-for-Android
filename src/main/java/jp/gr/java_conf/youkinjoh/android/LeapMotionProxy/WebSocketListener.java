package jp.gr.java_conf.youkinjoh.android.LeapMotionProxy;

import org.eclipse.jetty.websocket.WebSocket;

public class WebSocketListener implements WebSocket.OnControl, WebSocket.OnFrame, WebSocket.OnTextMessage, WebSocket.OnBinaryMessage {

	private WebSocketEmitter emitter = null;
	private final ConnectionType type;

	public WebSocketListener(ConnectionType type, WebSocketEmitter emitter) {
		super();
		this.emitter = emitter;
		this.type = type;
	}

	@Override
	public void onHandshake(FrameConnection connection) {
		emitter.onHandshake(type, connection);
	}

	@Override
	public void onOpen(Connection connection) {
		emitter.onOpen(type, connection);
	}

	@Override
	public boolean onFrame(byte flags, byte opcode, byte[] data, int offset, int length) {
		emitter.onFrame(type, flags, opcode, data, offset, length);
		return false;
	}

	@Override
	public boolean onControl(byte controlCode, byte[] data, int offset, int length) {
		emitter.onControl(type, controlCode, data, offset, length);
		return false;
	}

	@Override
	public void onClose(int closeCode, String message) {
		emitter.onClose(type, closeCode, message);
	}

	@Override
	public void onMessage(String data) {
		emitter.onMessage(type, data);
	}

	@Override
	public void onMessage(byte[] data, int offset, int length) {
		emitter.onMessage(type, data, offset, length);
	}

	public interface WebSocketEmitter {

		public void onHandshake(ConnectionType type, FrameConnection connection);

		public void onOpen(ConnectionType type, Connection connection);

		public boolean onFrame(ConnectionType type, byte flags, byte opcode, byte[] data, int offset, int length);

		public boolean onControl(ConnectionType type, byte controlCode, byte[] data, int offset, int length);

		public void onClose(ConnectionType type, int closeCode, String message);

		public void onMessage(ConnectionType type, String data);

		public void onMessage(ConnectionType type, byte[] data, int offset, int length);

	}

}
