package util;

import android.util.Log;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TCPClient {
  private Socket socket;
  private PrintWriter out;
  private BufferedReader in;

  public TCPClient() {
  }

  public void initialize(String ipAddress, int port, int timeout) throws IOException {
    socket = new Socket();
    socket.connect(new InetSocketAddress(ipAddress, port), timeout);
    out = new PrintWriter(socket.getOutputStream(), true);
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    printLog("连接完成");
  }

  public boolean IsInitialized(){
    return socket != null && out != null && in != null;
  }

  public void connect(String ipAddress, int port, int timeout) throws IOException {
    if (isConnectedV2()){
      printLog("存在连接，断开之前的连接");
      disconnect(); // 断开之前的连接
    }
    printLog("开始连接");
    initialize(ipAddress, port, timeout);
  }

  public void sendData(String data) {
    out.print(data);
  }

  public int sendDataWithReply(String data, int timeout) {
    try {
      out.print(data);
      out.flush();

      long startTime = System.currentTimeMillis();
      while (!in.ready()) {
        if (System.currentTimeMillis() - startTime > timeout) {
          Log.d("Syslog", "读取超时");
          return -1;
        }
      }
      String response = in.readLine(); // 读取并去除空白字符
      if (Character.isDigit(response.charAt(0))) {
        Log.d("Syslog", "收到服务器的响应: " + response);
        return Integer.parseInt(response);
      } else {
        Log.d("Syslog", "收到非数字响应: " + response);
        return -1; // 或者根据实际情况返回其他值
      }
    } catch (IOException e) {
      Log.e("TCPClient error: ", e.toString());
      return -1;
    }
  }

  public boolean isConnected() {
    if (socket == null || !socket.isConnected()) {
      return false;
    }

    try {
      socket.sendUrgentData(0);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  // isConnectedV2
  // 通过发送心跳包的方式来确认连接是否正常
  // 发送格式：ping 接受格式：heartbeat
  public boolean isConnectedV2(){
    if (socket == null || !socket.isConnected()) {
      printLog("socket is null or socket is not connected: " + socket);
      return false;
    }
    synchronized (in) {
      printLog("开始检测连接");
      int connectionTimeOut = 1000;
      try {
        out.print("ping");
        out.flush();
        printLog("发送 ping");
        String response = in.readLine();
        printLog("收到response: " + response);
        if ("heartbeat".equals(response)) {
          printLog("Heartbeat received. Connection is active.");
          return true;
        } else {
          printLog("Unexpected response: " + response);
          return false;
        }
      } catch (IOException e) {
        printLog("Error sending/receiving heartbeat: " + e.getMessage());
        return false;
      }
    }
  }

  public void disconnect() throws IOException {
    if (socket != null && !socket.isClosed()) {
      out.close();
      in.close();
      socket.close();
    }
  }

  private void printLog(String msg) {
    Log.d("Syslog", msg);
  }
}