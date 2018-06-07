package east.orientation.caster.soket;

import android.util.Log;


import com.xuhao.android.libsocket.utils.BytesUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Socket收发器 通过Socket发送数据，并使用新线程监听Socket接收到的数据
 */
public abstract class SocketTransceiver implements Runnable {
	private static final String TAG = "SocketTransceiver";

	private static final int DEFAULT_PACKET_LEN = 1024*10;
	private Socket socket;
	private InetAddress addr;
	private DataInputStream in;
	private DataOutputStream out;
	private boolean runFlag;

	/**
	 * 实例化
	 *
	 * @param socket
	 *            已经建立连接的socket
	 */
	public SocketTransceiver(Socket socket) {
		this.socket = socket;
		this.addr = socket.getInetAddress();
	}

	/**
	 * 获取连接到的Socket地址
	 *
	 * @return InetAddress对象
	 */
	public InetAddress getInetAddress() {
		return addr;
	}

	/**
	 * 开启Socket收发
	 * <p>
	 * 如果开启失败，会断开连接并回调{@code onDisconnect()}
	 */
	public void start() {
		runFlag = true;
		new Thread(this).start();
	}

	/**
	 * 断开连接(主动)
	 * <p>
	 * 连接断开后，会回调{@code onDisconnect()}
	 */
	public void stop() {
		runFlag = false;
		try {
			socket.shutdownInput();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送字节数组
	 *
	 * @param bytes 字节数组
	 *
	 * @return 发送成功返回true
	 */
	public boolean send(byte[] bytes){
		if (out != null) {
			try {
				Log.e(TAG,"send bytes length "+bytes.length);
				out.write(bytes);
				out.flush();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG,"Send error : "+e);
			}
		}
		return false;
	}

	/**
	 * 发送字符串
	 *
	 * @param str
	 *
	 * @return 发送成功返回true
	 */
	public boolean send(String str){
		if (out != null) {
			try {
				out.write(BytesUtils.intToBytes(str.getBytes("gbk").length));
				Log.e(TAG,"send str length "+str);
				out.write(str.getBytes("gbk"));

				out.flush();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG,"Send error : "+e);
			}
		}
		return false;
	}

	/**
	 * 上传文件
	 *
	 * @param str	请求
	 *
	 * @param fileSize	文件大小
	 *
	 * @return
	 */
	public boolean send(String str ,int fileSize){
		if (out != null) {
			try {
				out.write(BytesUtils.intToBytes(str.getBytes("gbk").length+fileSize));
				Log.e(TAG,"send str length "+str);
				out.write(str.getBytes("gbk"));

				out.flush();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG,"Send error : "+e);
			}
		}
		return false;
	}

	/**
	 * 监听Socket接收的数据(新线程中运行)
	 */
	@Override
	public void run() {
		try {
			in = new DataInputStream(this.socket.getInputStream());
			out = new DataOutputStream(this.socket.getOutputStream());

		} catch (IOException e) {
			e.printStackTrace();
			runFlag = false;
		}
		while (runFlag) {
			try {
				byte[] lenBytes = new byte[4];
				in.readFully(lenBytes);
				int len = BytesUtils.bytesToInt(lenBytes,0);
				Log.e(TAG,"head len: "+len);
				if (len>0){
					if (len>DEFAULT_PACKET_LEN){
						double c = len/DEFAULT_PACKET_LEN;
						while(c-- > 0){
							byte[] frame = new byte[DEFAULT_PACKET_LEN];
							in.readFully(frame);
							this.onReceive(addr,frame);
						}

						int tail = len % DEFAULT_PACKET_LEN;
						byte[] tailBytes = new byte[tail];
						in.readFully(tailBytes);
						this.onReceive(addr,tailBytes);

					}else {
						byte[] frame = new byte[len];
						in.readFully(frame);
						//Log.e(TAG,len+"run <= default"+frame.length);
						this.onReceive(addr, frame);
					}
				}

			} catch (IOException e) {
				// 连接被断开(被动)
				Log.e(TAG,"DisConnect error "+e);
				runFlag = false;
			}
		}
		// 断开连接
		try {
			in.close();
			out.close();
			socket.close();
			in = null;
			out = null;
			socket = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.onDisconnect(addr);
	}

	/**
	 * 接收到数据
	 * <p>
	 * 注意：此回调是在新线程中执行的
	 *
	 * @param addr
	 *            连接到的Socket地址
	 * @param frame
	 *            收到的字符串
	 */
	public abstract void onReceive(InetAddress addr, byte[] frame);

	/**
	 * 连接断开
	 * <p>
	 * 注意：此回调是在新线程中执行的
	 *
	 * @param addr
	 *            连接到的Socket地址
	 */
	public abstract void onDisconnect(InetAddress addr);
}
