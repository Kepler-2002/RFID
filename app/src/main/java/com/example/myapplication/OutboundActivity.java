package com.example.myapplication;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutboundActivity extends AppCompatActivity {

  private TextView textViewReadCount;
  private TableLayout tableLayout;
  private TableLayout summaryTableLayout;
  private Button buttonReadRFID;
  private Button buttonOutbound;
  private int readCount = 0; // 读取数量

  private List<RFIDData> rfidDataList = new ArrayList<>(); // 存储RFID读取结果


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_outbound);

    // 初始化视图组件
    textViewReadCount = findViewById(R.id.textViewReadCount);
    tableLayout = findViewById(R.id.tableLayout);
    summaryTableLayout = findViewById(R.id.summaryTableLayout);
    buttonReadRFID = findViewById(R.id.buttonReadRFID);
    buttonOutbound = findViewById(R.id.buttonOutbound);

    // 设置读取RFID按钮的点击事件
    buttonReadRFID.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // 处理读取RFID操作
        // 在这里执行读取RFID的操作，然后将读取到的信息添加到表格中
        // 读取过程中禁用按钮
        buttonReadRFID.setEnabled(false);
        // 模拟读取操作，5秒后启用按钮并显示读取信息
        simulateRFIDRead();
      }
    });

    // 设置出库按钮的点击事件
    buttonOutbound.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // 处理出库操作
        // 在这里执行出库操作，将读取到的信息添加到汇总表格中
        // 出库成功后禁用按钮
        buttonOutbound.setEnabled(false);


        // 显示读取信息到汇总表格中
        summarizeAndShowData();


      }
    });
  }

  // 模拟RFID读取操作
  private void simulateRFIDRead() {
    // 清空表格
    clearTable();
    buttonOutbound.setEnabled(false);

    // 模拟读取操作，5秒后启用按钮并显示读取信息
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(5000); // 模拟5秒的读取时间
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            buttonReadRFID.setEnabled(true);
            // 更新读取数量
            readCount += 10; // 模拟读取到的数量
            textViewReadCount.setText("读取数量: " + readCount);

            // 模拟读取结果，将读取到的数据添加到列表中
            for (int i = 0; i < 10; i++) {
              rfidDataList.add(new RFIDData(String.valueOf(i),"单位" + (i % 3 + 1), "部门" + (i % 3 + 1), "人员" + (i % 3 + 1), "规格" + (i % 2 + 1), "产品" + (i % 2 + 1)));
            }

            // 显示读取信息到表格中（示例，实际操作需要根据读取结果进行处理）
            showReadDataInTable();

            // 启用出库按钮
            buttonOutbound.setEnabled(true);
          }
        });
      }
    }).start();
  }

  // 清空表格
  private void clearTable() {
    // 清空读取数量
    readCount = 0;
    textViewReadCount.setText("读取数量: 0");
    int childCount = tableLayout.getChildCount();
    // 从索引1开始，以避免移除表头
    for (int i = 1; i < childCount; i++) {
      tableLayout.removeViewAt(1);
    }
  }

  private void clearSummaryTable() {

    int childCount = summaryTableLayout.getChildCount();
    // 从索引1开始，以避免移除表头
    for (int i = 1; i < childCount; i++) {
      summaryTableLayout.removeViewAt(1);
    }
  }

// 显示读取信息到表格中
private void showReadDataInTable() {
  for (RFIDData rfidData : rfidDataList) {
    // 创建新的表格行
    TableRow row = new TableRow(this);

    // 创建并添加RFID编号的TextView
    TextView rfidTextView = new TextView(this);
    rfidTextView.setText(rfidData.getID()); // 替换为实际的RFID编号
    row.addView(rfidTextView);

    // 创建并添加产品名称的TextView
    TextView productNameTextView = new TextView(this);
    productNameTextView.setText(rfidData.getProductName());
    row.addView(productNameTextView);

    // 创建并添加规格型号的TextView
    TextView specTextView = new TextView(this);
    specTextView.setText(rfidData.getSpec());
    row.addView(specTextView);

    // 创建并添加单位的TextView
    TextView unitTextView = new TextView(this);
    unitTextView.setText(rfidData.getUnit());
    row.addView(unitTextView);

    // 创建并添加部门的TextView
    TextView departmentTextView = new TextView(this);
    departmentTextView.setText(rfidData.getDepartment());
    row.addView(departmentTextView);

    // 创建并添加人员的TextView
    TextView personTextView = new TextView(this);
    personTextView.setText(rfidData.getPerson());
    row.addView(personTextView);

    // 将行添加到表格中
    tableLayout.addView(row);
  }
}

  // 显示读取信息到汇总表格中
  private void summarizeAndShowData() {
    clearSummaryTable();
    Map<String, Integer> summaryData = new HashMap<>();

    // 遍历RFID数据列表，统计出库数量
    for (RFIDData rfidData : rfidDataList) {
      String key = rfidData.getUnit() + ","+rfidData.getDepartment() +","+ rfidData.getPerson()+","
               +rfidData.getSpec() + "," + rfidData.getProductName();
      if (summaryData.containsKey(key)) {
        summaryData.put(key, summaryData.get(key) + 1);
      } else {
        summaryData.put(key, 1);
      }
    }



    // 遍历汇总数据并显示在汇总表格中
    for (Map.Entry<String, Integer> entry : summaryData.entrySet()) {
      String[] keys = entry.getKey().split(",");
      String productName = keys[0];
      String spec = keys[1];
      String unit = keys[2];
      String department = keys[3];
      String person = keys[4];
      int quantity = entry.getValue();

      TableRow summaryRow = new TableRow(this);

      TextView productNameTextView = new TextView(this);
      productNameTextView.setText(productName);
      summaryRow.addView(productNameTextView);

      TextView specTextView = new TextView(this);
      specTextView.setText(spec);
      summaryRow.addView(specTextView);

      TextView unitTextView = new TextView(this);
      unitTextView.setText(unit);
      summaryRow.addView(unitTextView);

      TextView departmentTextView = new TextView(this);
      departmentTextView.setText(department);
      summaryRow.addView(departmentTextView);

      TextView personTextView = new TextView(this);
      personTextView.setText(person);
      summaryRow.addView(personTextView);

      TextView quantityTextView = new TextView(this);
      quantityTextView.setText(String.valueOf(quantity));
      summaryRow.addView(quantityTextView);

      summaryTableLayout.addView(summaryRow);
    }
  }

  // 显示 Toast 消息的方法
  private void showToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  // 创建选项菜单
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_home, menu);
    return true;
  }

  // 处理选项菜单项的点击事件
  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.menu_logout) {
      // 处理退出登录操作
      showToast("退出登录");
      // 这里可以执行退出登录的相关逻辑，如清除用户登录状态等

      // 结束当前Activity并返回到登录界面
      finish();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

}
