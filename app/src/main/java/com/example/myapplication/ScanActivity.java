package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.rfidread.Enumeration.eGPOState;
import util.ConnectToReaderTask;
import util.RFIDReaderHelper;
import util.TCPClient;

import java.io.IOException;
import java.util.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.concurrent.*;

public class ScanActivity extends AppCompatActivity{
  private RFIDReaderHelper rfidReaderHelper;

  public TCPClient tcpClient = new TCPClient();
  private String defaultIpAddress;
  private int defaultPort; // 默认端口号

  private String connID;

  private Spinner spinnerPower;
  private TextView textViewReadCount;

  private TextView text1;

  private TableLayout tableLayout;

  private Button buttonRefresh;
  private EditText editTextIpAddress;
  private Button buttonConnect;

  private ProgressBar progressBarConnecting;
  private TextView textConnectingStatus;

  private boolean isReconnectRunning = false;

  // 不同的选择框选项数据
  private String [] powerOptions = new String[30];

  // 声明一个Handler用于定时任务
  private Handler handler = new Handler();

  private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private Runnable reconnectRunnable = new Runnable() {
    @Override
    public void run() {
      if (!isReconnectRunning) {
        isReconnectRunning = true;
      }

      boolean isConnected = tcpClient.isConnectedV2();

      if (!isConnected) {
        Log.d("Syslog", "TCP连接断开，正在连接...");
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            showToast("TCP连接断开，正在重新连接...");
          }
        });

        // 进行重新连接操作
        connectToTCPClient(defaultIpAddress, defaultPort);
      }
    }
  };

  BlockingDeque<String> buffer = new LinkedBlockingDeque<>();


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    rfidReaderHelper = new RFIDReaderHelper(buffer, new HashMap<>(), this);

    // 创建一个线程用于从缓冲区的头部获取数据并发送
    new Thread(() -> {
      while (true) {
        if (tcpClient.IsInitialized()){
          try {
            String data = buffer.takeFirst();
            int response = tcpClient.sendDataWithReply(data, 5000);
            if (response != 1) {
              Log.d("Syslog","Send data failed: " + data + " response: " + response);
              buffer.putFirst(data); // 发送失败，将数据放回队列的头部
            }else {
              Log.d("Syslog","Send data success");
            }

            Thread.sleep(200);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }).start();

    Objects.requireNonNull(getSupportActionBar()).hide();
    setContentView(R.layout.activity_scan);

    // 获取ProgressBar引用
    progressBarConnecting = findViewById(R.id.progressBarConnecting);
    textConnectingStatus = findViewById(R.id.textConnectingStatus);

    // 从SharedPreferences中读取默认IP地址和端口号
    SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    defaultIpAddress = sharedPref.getString("ipAddress", "192.168.1.105");
    defaultPort = sharedPref.getInt("port", 9999);

    // 初始化视图组件
    spinnerPower= findViewById(R.id.spinnerPower);
    textViewReadCount = findViewById(R.id.textViewReadCount);
    buttonRefresh = findViewById(R.id.buttonRefresh);
    tableLayout = findViewById(R.id.tableLayout);
    editTextIpAddress = findViewById(R.id.editTextIpAddress);
    editTextIpAddress.setText(defaultIpAddress + ":" + defaultPort);
    buttonConnect = findViewById(R.id.buttonConnect);
    text1 = findViewById(R.id.text1);

    ImageView imageViewLogo = findViewById(R.id.imageViewLogo);
    imageViewLogo.setImageResource(R.drawable.logo); // 设置logo图片资源

    // 显示进度条
    progressBarConnecting.setVisibility(View.VISIBLE);
    textConnectingStatus.setVisibility(View.VISIBLE);
    textConnectingStatus.setText("正在连接读写器");

    spinnerPower.setVisibility(View.INVISIBLE);
    textViewReadCount.setVisibility(View.INVISIBLE);
    buttonRefresh.setVisibility(View.INVISIBLE);
    tableLayout.setVisibility(View.INVISIBLE);
    editTextIpAddress.setVisibility(View.INVISIBLE);
    buttonConnect.setVisibility(View.INVISIBLE);
    text1.setVisibility(View.INVISIBLE);

    // 执行读写器连接操作
    ConnectToReaderTask connectTask = new ConnectToReaderTask(this, rfidReaderHelper); // 传递活动的上下文
    connectTask.execute();


    // 设置连接按钮的点击事件
    buttonConnect.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // 在这里处理连接操作
        // 获取输入框中的IP地址和端口号
        String ipAddressPort = editTextIpAddress.getText().toString();
        String[] parts = ipAddressPort.split(":");


        if (parts.length == 2) {
          String ipAddress = parts[0];
          int port = Integer.parseInt(parts[1]);

          connectToTCPClient(ipAddress,port);


          if (!isReconnectRunning) {
            Log.d("Syslog", "call detection method");
            scheduler.scheduleAtFixedRate(reconnectRunnable, 0, 5, TimeUnit.SECONDS);
          }

          // 将获取的IP地址和端口号设置为默认值
          defaultIpAddress = ipAddress;
          defaultPort = port;

          editTextIpAddress.setText(defaultIpAddress + ":" + defaultPort);


          // 保存新的默认IP地址和端口号到SharedPreferences
          SharedPreferences.Editor editor = sharedPref.edit();
          editor.putString("ipAddress", ipAddress);
          editor.putInt("port", port);
          editor.apply(); // 提交更改
          editor.commit();
        } else {
          showToast("请输入有效的 IP 地址和端口号（格式：IP:端口号）");
        }
      }
    });

    buttonRefresh.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // 处理提交操作
        // 在这里执行提交操作，清空表格等操作
        // 提交成功后禁用按钮
        buttonRefresh.setEnabled(false);
        // 清空表格
        clearTable();

        buttonRefresh.setEnabled(true);
      }
    });

  }
  private ExecutorService executorService = Executors.newSingleThreadExecutor();

  private void connectToTCPClient(String targetIpAddress, int targetPort) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (tcpClient != null) {
            tcpClient.connect(targetIpAddress, targetPort,5000);
          } else {
            tcpClient = new TCPClient();
            tcpClient.connect(targetIpAddress, targetPort,5000);
          }
          if (tcpClient.isConnectedV2()) {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Log.d("SysLog", "TCP Connection successful");
                Toast.makeText(ScanActivity.this, "TCP连接建立成功", Toast.LENGTH_SHORT).show();
              }
            });
          }
        } catch (final IOException e) {
          e.printStackTrace();
        }
      };
    });
  }

  // 初始化选择框
  private void initSpinners(int defaultPowerLevel) {

    int defaultPowerLevelPosition = 0;

    for(int i = 1; i <= 30; i++){
      powerOptions[i - 1] = String.valueOf(i);
      if (i == defaultPowerLevel) {
        // 设置默认显示值为当前功率等级
        defaultPowerLevelPosition = i - 1;
      }
    }

    // 为每个选择框设置不同的选项数据源
    ArrayAdapter<String> powerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, powerOptions);

    // 设置下拉列表风格
    powerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // 为每个选择框设置适配器
    spinnerPower.setAdapter(powerAdapter);
    // 设置默认选中的功率等级
    Log.d("System.out","defaultPowerLevelPosition: " + defaultPowerLevelPosition);
    spinnerPower.setSelection(defaultPowerLevelPosition);

    // 在选择框的初始化中添加选择监听器
    spinnerPower.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        // 获取用户选择的功率等级（注意，这里的position是从0开始的，所以需要加1）
        int selectedPowerLevel = position + 1;

        // 调用设置功率的函数
        int result = rfidReaderHelper.setPowerLevel(selectedPowerLevel);
        if (result == 0) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              // 保存新的默认功率值到SharedPreferences
              SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
              SharedPreferences.Editor editor = sharedPref.edit();
              editor.putInt("defaultPowerLevel", selectedPowerLevel);
              editor.apply(); // 提交更改

              String currentPowerLevel1 = rfidReaderHelper.getPowerLevel(connID);
              Log.d("System.out", "CurrentPowerLevel: " + currentPowerLevel1);
              Toast.makeText(ScanActivity.this, "设置功率成功，当前功率等级：" + currentPowerLevel1, Toast.LENGTH_SHORT).show();
            }
          });
        } else {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              String currentPowerLevel1 = rfidReaderHelper.getPowerLevel(connID);
              Toast.makeText(ScanActivity.this, "设置功率失败，当前功率等级：" + currentPowerLevel1, Toast.LENGTH_SHORT).show();
            }
          });
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parentView) {
        // 选择框未选择时的处理，可以留空或添加逻辑
      }
    });


  }

  public void showConnectingProgress() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        progressBarConnecting.setVisibility(View.VISIBLE);
      }
    });
  }

  public void hideConnectingProgress() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        progressBarConnecting.setVisibility(View.GONE);
      }
    });
  }

  public void initUI() {
    if (rfidReaderHelper.conn){
      // 从SharedPreferences中读取默认IP地址和端口号
      SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
      defaultIpAddress = sharedPref.getString("ipAddress", "192.168.1.105");
      defaultPort = sharedPref.getInt("port", 9999);
      int defaultPowerLevel = sharedPref.getInt("defaultPowerLevel", 30);
      Log.d("System.out", "Default Power Level: " + defaultPowerLevel);
      // 初始化选择框
      initSpinners(defaultPowerLevel);
    }

    if(!isReconnectRunning) {
      // 启动定时任务，每5秒执行一次
      scheduler.scheduleAtFixedRate(reconnectRunnable, 0, 5, TimeUnit.SECONDS);
    }

    // 继续初始化界面的代码
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        spinnerPower.setVisibility(View.VISIBLE);
        textViewReadCount.setVisibility(View.VISIBLE);
        buttonRefresh.setVisibility(View.VISIBLE);
        tableLayout.setVisibility(View.VISIBLE);
        editTextIpAddress.setVisibility(View.VISIBLE);
        buttonConnect.setVisibility(View.VISIBLE);
        text1.setVisibility(View.VISIBLE);
        textConnectingStatus.setVisibility(View.INVISIBLE);
      }
    });
  }

  public void showConnectionErrorDialog() {
    // 连接失败，显示警告对话框
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
        builder.setMessage("连接读写器失败，请联系技术支持")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                    // 退出应用程序
                    finish();
                  }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
      }
    });
  }

  // 清空表格
  private void clearTable() {
    int childCount = tableLayout.getChildCount();
    // 从索引1开始，以避免移除表头
    for (int i = 1; i < childCount; i++) {
      tableLayout.removeViewAt(1);
    }

    // 清空uniqueEpcData
    rfidReaderHelper.EpcDataMap.clear();
    rfidReaderHelper.EpcDataList.clear();

    textViewReadCount.setText("读取数量: " + "0"); // 更新显示的读取数量

  }

  // 显示 Toast 消息的方法
  public void showToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  // 在表格中插入一行数据
  public void insertRowInTable(String epcData) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        // 在这里执行需要在主线程上进行的UI操作
        TableRow row = new TableRow(ScanActivity.this);
        TextView textViewRFID = new TextView(ScanActivity.this);

        // 设置每列的文本内容，这里需要根据实际数据进行设置
        textViewRFID.setText(epcData);
        textViewRFID.setTextSize(24);

        // 添加到表格行
        row.addView(textViewRFID);

        // 添加到表格
        tableLayout.addView(row);


      }
    });
  }


  // 更新读取数量
  public void updateReadCount() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        int currentReadCount = Integer.parseInt(textViewReadCount.getText().toString().split(" ")[1]); // 获取当前读取数量
        currentReadCount++; // 增加读取数量
        textViewReadCount.setText("读取数量: " + currentReadCount); // 更新显示的读取数量
      }
    });

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    rfidReaderHelper.closeAllConnect();
    executorService.shutdown();
    scheduler.shutdown();
  }
}

