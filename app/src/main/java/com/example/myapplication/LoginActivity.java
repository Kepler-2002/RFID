package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.rfidread.Interface.IAsynchronousMessage;
import com.rfidread.Models.GPI_Model;
import com.rfidread.Models.Tag_Model;
import com.rfidread.RFIDReader;
public class LoginActivity extends AppCompatActivity implements IAsynchronousMessage {

  private String [] Ports = {
              "/dev/ttyUSB2",
              "/dev/ttyUSB1",
              "/dev/ttyUSB0",
              "/dev/ttyS8",
              "/dev/ttyS7",
              "/dev/ttyS6",
              "/dev/ttyS5",
              "/dev/ttyS4",
              "/dev/ttyS3",
              "/dev/ttyS2",
              "/dev/ttyS1",
              "/dev/ttyS0"};

  private EditText editTextUsername, editTextPassword;
  private Button buttonLogin;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化视图组件
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);




        // 设置登录按钮的点击事件
        buttonLogin.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            // 获取用户输入的用户名和密码
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // 进行用户名和密码验证（这里可以添加自己的验证逻辑）
            if (isValid(username, password)) {
    //          // 验证通过，可以执行登录操作
    //          Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();

              // 这里可以跳转到其他界面或执行其他操作
              // 验证通过，启动HomeActivity
              Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
              startActivity(intent);
            } else {
              // 验证失败，显示错误提示
              Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
            }
          }
        });
  }

  @Override
  protected void onStart(){
    super.onStart();


    editTextUsername.setText("链接失败");
    for (int i = 0; i < Ports.length; i++) {
      boolean conn = RFIDReader.CreateSerialConn(Ports[i] + ":115200", this);

      if (conn){
        editTextUsername.setText("连接成功");
      }
    }
  }

  // 自定义的用户名和密码验证逻辑
  private boolean isValid(String username, String password) {
    // 这里可以添加你的验证逻辑，例如与服务器进行验证
    //    return username.equals("username") && password.equals("password");
    return true;
  }



  @Override
  public void WriteDebugMsg(String s, String s1) {

  }

  @Override
  public void WriteLog(String s, String s1) {

  }

  @Override
  public void PortConnecting(String s) {

  }

  @Override
  public void PortClosing(String s) {

  }

  @Override
  public void OutPutTags(Tag_Model tag_model) {

  }

  @Override
  public void OutPutTagsOver(String s) {

  }

  @Override
  public void GPIControlMsg(String s, GPI_Model gpi_model) {

  }

  @Override
  public void OutPutScanData(String s, byte[] bytes) {

  }
}
