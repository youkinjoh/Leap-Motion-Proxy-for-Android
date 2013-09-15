package jp.gr.java_conf.youkinjoh.android.LeapMotionProxy;

public class StackMessage {

	private TextData textData = null;
	private ByteData byteData = null;
	private boolean isByte = false;

	public StackMessage(TextData data) {
		isByte = false;
		this.textData = data;
	}

	public StackMessage(ByteData data) {
		isByte = true;
		this.byteData = data;
	}
	
	TextData getTextData() {
		return textData;
	}

	ByteData getByteData() {
		return byteData;
	}

	boolean isText() {
		return !isByte;
	}

	boolean isByte() {
		return isByte;
	}

}

class TextData {

	private String data = null;

	public TextData(String data) {
		this.data = data;
	}

	String getData() {
		return data;
	}

}

class ByteData {

	private byte[] data = null;
	private int offset = -1;
	private int length = -1;

	public ByteData(byte[] data, int offset, int length) {
		this.data = data;
		this.offset = offset;
		this.length = length;
	}

	byte[] getData() {
		return data;
	}

	int getOffSet() {
		return offset;
	}

	int getLength() {
		return length;
	}

}
