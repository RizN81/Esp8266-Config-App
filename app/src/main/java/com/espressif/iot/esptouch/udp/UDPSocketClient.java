package com.espressif.iot.esptouch.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.espressif.iot.esptouch.task.IEsptouchTask;

import android.util.Log;

/**
 * this class is used to help send UDP data according to length
 * 
 * @author afunx
 * 
 */
public class UDPSocketClient {

	private static final String TAG = "UDPSocketClient";
	private DatagramSocket mSocket;
	private volatile boolean mIsStop;
	private volatile boolean mIsClosed;

	public UDPSocketClient() {
		try {
			this.mSocket = new DatagramSocket();
			this.mIsStop = false;
			this.mIsClosed = false;
		} catch (SocketException e) {
			if ( IEsptouchTask.DEBUG) {
				Log.e(TAG, "SocketException");
			}
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public void interrupt() {
		if ( IEsptouchTask.DEBUG) {
			Log.i(TAG, "USPSocketClient is interrupt");
		}
		this.mIsStop = true;
	}

	/**
	 * close the UDP socket
	 */
	public synchronized void close() {
		if (!this.mIsClosed) {
			this.mSocket.close();
			this.mIsClosed = true;
		}
	}

	/**
	 * send the data by UDP
	 * 
	 * @param data
	 *            the data to be sent
	 * @param targetHost
	 *            the host name of target, e.g. 192.168.1.101
	 * @param targetPort
	 *            the port of target
	 * @param interval
	 *            the milliseconds to between each UDP sent
	 */
	public void sendData(byte[][] data, String targetHostName, int targetPort,
			long interval) {
		sendData(data, 0, data.length, targetHostName, targetPort, interval);
	}
	
	
	/**
	 * send the data by UDP
	 * 
	 * @param data
	 *            the data to be sent
	 * @param offset
	 * 			  the offset which data to be sent
	 * @param count
	 * 			  the count of the data
	 * @param targetHost
	 *            the host name of target, e.g. 192.168.1.101
	 * @param targetPort
	 *            the port of target
	 * @param interval
	 *            the milliseconds to between each UDP sent
	 */
	public void sendData(byte[][] data, int offset, int count,
			String targetHostName, int targetPort, long interval) {
		if ((data == null) || (data.length <= 0)) {
			if ( IEsptouchTask.DEBUG) {
				Log.e(TAG, "sendData(): data == null or length <= 0");
			}
			return;
		}
		for (int i = offset; !mIsStop && i < offset + count; i++) {
			if (data[i].length == 0) {
				continue;
			}
			try {
				// Log.i(TAG, "data[" + i + " +].length = " + data[i].length);
				DatagramPacket localDatagramPacket = new DatagramPacket(
						data[i], data[i].length,
						InetAddress.getByName(targetHostName), targetPort);
				this.mSocket.send(localDatagramPacket);
			} catch (UnknownHostException e) {
				if ( IEsptouchTask.DEBUG) {
					Log.e(TAG, "sendData(): UnknownHostException");
				}
				e.printStackTrace();
				mIsStop = true;
				break;
			} catch (IOException e) {
				if ( IEsptouchTask.DEBUG) {
					Log.e(TAG, "sendData(): IOException, but just ignore it");
				}
				// for the Ap will make some troubles when the phone send too many UDP packets,
	            // but we don't expect the UDP packet received by others, so just ignore it
			}
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
				if ( IEsptouchTask.DEBUG) {
					Log.e(TAG, "sendData is Interrupted");
				}
				mIsStop = true;
				break;
			}
		}
		if (mIsStop) {
			close();
		}
	}
}
