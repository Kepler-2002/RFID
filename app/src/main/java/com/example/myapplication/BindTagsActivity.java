package com.example.myapplication;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class BindTagsActivity extends AppCompatActivity {

  private Spinner spinnerUnit, spinnerDepartment, spinnerPerson, spinnerCategory, spinnerSpecification;
  private TextView textViewReadCount;
  private TableLayout tableLayout;
  private Button buttonReadRFID, buttonSubmit;

  // 不同的选择框选项数据
  private String[] unitOptions = {"医院1", "医院2", "医院3"};
  private String[] departmentOptions = {"科室1", "科室2", "科室3"};
  private String[] personOptions = {"人员1", "人员2", "人员3"};
  private String[] categoryOptions = {"类别1", "类别2", "类别3"};
  private String[] specificationOptions = {"规格1", "规格2", "规格3"};

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_bind_tags);

    // 初始化视图组件
    spinnerUnit = findViewById(R.id.spinnerUnit);
    spinnerDepartment = findViewById(R.id.spinnerDepartment);
    spinnerPerson = findViewById(R.id.spinnerPerson);
    spinnerCategory = findViewById(R.id.spinnerCategory);
    spinnerSpecification = findViewById(R.id.spinnerSpecification);
    textViewReadCount = findViewById(R.id.textViewReadCount);
    tableLayout = findViewById(R.id.tableLayout);
    buttonReadRFID = findViewById(R.id.buttonReadRFID);
    buttonSubmit = findViewById(R.id.buttonSubmit);

    // 初始化选择框
    initSpinners();

    // 设置读取RFID按钮的点击事件
    buttonReadRFID.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // 处理读取RFID操作
        // 在这里执行读取RFID的操作，然后将读取到的信息添加到表格中
        // 读取过程中禁用按钮
        buttonReadRFID.setEnabled(false);
        // 模拟读取操作，3秒后启用按钮并显示读取信息
        simulateRFIDRead();
      }
    });

    // 设置提交按钮的点击事件
    buttonSubmit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // 处理提交操作
        // 在这里执行提交操作，清空表格等操作
        // 提交成功后禁用按钮
        buttonSubmit.setEnabled(false);
        // 清空表格
        clearTable();
      }
    });
  }

  // 初始化选择框
  private void initSpinners() {
    // 为每个选择框设置不同的选项数据源
    ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unitOptions);
    ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departmentOptions);
    ArrayAdapter<String> personAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, personOptions);
    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryOptions);
    ArrayAdapter<String> specificationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, specificationOptions);

    // 设置下拉列表风格
    unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    personAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    specificationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    // 为每个选择框设置适配器
    spinnerUnit.setAdapter(unitAdapter);
    spinnerDepartment.setAdapter(departmentAdapter);
    spinnerPerson.setAdapter(personAdapter);
    spinnerCategory.setAdapter(categoryAdapter);
    spinnerSpecification.setAdapter(specificationAdapter);
  }

  // 模拟RFID读取操作
  private void simulateRFIDRead() {

    // 清空表格
    clearTable();
    buttonSubmit.setEnabled(false);

    // 模拟读取操作，3秒后启用按钮并显示读取信息
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(3000); // 模拟3秒的读取时间
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            // 启用按钮
            buttonReadRFID.setEnabled(true);
            // 更新读取数量
            int readCount = 10; // 模拟读取到的数量
            textViewReadCount.setText("读取数量: " + readCount);

            // 显示读取信息到表格中（示例，实际操作需要根据读取结果进行处理）
            showReadDataInTable(readCount);

            buttonSubmit.setEnabled(true);
          }
        });
      }
    }).start();
  }

  // 显示读取信息到表格中
  private void showReadDataInTable(int readCount) {

    // 在这里根据读取到的数据将信息显示到表格中
    // 这里只是一个示例，你需要根据实际情况进行处理
    for (int i = 0; i < readCount; i++) {
      TableRow row = new TableRow(this);
      TextView textViewRFID = new TextView(this);
      TextView textViewProductName = new TextView(this);
      TextView textViewSpecification = new TextView(this);
      TextView textViewUnit = new TextView(this);
      TextView textViewDepartment = new TextView(this);
      TextView textViewPerson = new TextView(this);

      // 设置每列的文本内容，这里需要根据实际数据进行设置
      textViewRFID.setText("RFID" + i);
      textViewProductName.setText("产品名称" + i);
      textViewSpecification.setText("规格型号" + i);
      textViewUnit.setText("单位" + i);
      textViewDepartment.setText("部门" + i);
      textViewPerson.setText("人员" + i);

      // 添加到表格行
      row.addView(textViewRFID);
      row.addView(textViewProductName);
      row.addView(textViewSpecification);
      row.addView(textViewUnit);
      row.addView(textViewDepartment);
      row.addView(textViewPerson);

      // 添加到表格
      tableLayout.addView(row);
    }
  }

  // 清空表格
  private void clearTable() {
    int childCount = tableLayout.getChildCount();
    // 从索引1开始，以避免移除表头
    for (int i = 1; i < childCount; i++) {
      tableLayout.removeViewAt(1);
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

