package util;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class TCPServer implements Runnable {
    private static final String TAG = "TCPServer";
    private int port;
    private boolean isRunning;
    private BlockingQueue<String> plcDataQueue;
    private ServerSocket serverSocket;
    private Socket currentClientSocket;
    private String clientIpAddress;

    public TCPServer(int port, BlockingQueue<String> plcDataQueue) {
        this.port = port;
        this.plcDataQueue = plcDataQueue;
    }

    public void start() {
        isRunning = true;
        new Thread(this).start();
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing server socket", e);
        }
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            Log.d(TAG, "TCP Server started on port: " + port);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                Log.d(TAG, "PLC client connected: " + clientSocket.getInetAddress().getHostAddress());
                
                // 保存当前客户端连接信息
                currentClientSocket = clientSocket;
                clientIpAddress = clientSocket.getInetAddress().getHostAddress();
                
                handleClient(clientSocket);
                
                // 客户端断开连接后清除信息
                currentClientSocket = null;
                clientIpAddress = null;
            }
        } catch (IOException e) {
            if (isRunning) {
                Log.e(TAG, "Server error", e);
            } else {
                Log.d(TAG, "Server stopped.");
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (InputStream inputStream = clientSocket.getInputStream()) {
            byte[] buffer = new byte[6];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                if (bytesRead == 6 && buffer[0] == 0x02 && buffer[5] == 0x03) {
                    String carId = new String(buffer, 1, 4);
                    Log.d(TAG, "Received car ID: " + carId);
                    plcDataQueue.offer(carId);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error handling client", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing client socket", e);
            }
        }
    }

    // 获取当前客户端连接状态
    public boolean isClientConnected() {
        return currentClientSocket != null && !currentClientSocket.isClosed() && currentClientSocket.isConnected();
    }

    // 获取当前客户端IP地址
    public String getClientIpAddress() {
        return clientIpAddress;
    }
}