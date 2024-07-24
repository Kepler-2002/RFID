package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;




public class HomeActivity extends AppCompatActivity {


  private Button buttonBindTag, buttonInbound, buttonOutbound;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    // 初始化视图组件
    buttonBindTag = findViewById(R.id.buttonBindTag);
    buttonInbound = findViewById(R.id.buttonInbound);
    buttonOutbound = findViewById(R.id.buttonOutbound);

    // 设置标签绑定按钮的点击事件
    buttonBindTag.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // 处理标签绑定操作，这里只显示一个Toast示例
        Toast.makeText(HomeActivity.this, "标签绑定功能", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HomeActivity.this, BindTagsActivity.class);
        startActivity(intent);
      }
    });

    // 设置入库按钮的点击事件
    buttonInbound.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // 处理入库操作，这里只显示一个Toast示例
        Toast.makeText(HomeActivity.this, "入库功能", Toast.LENGTH_SHORT).show();
      }
    });

    // 设置出库按钮的点击事件
    buttonOutbound.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // 处理出库操作，这里只显示一个Toast示例
        Toast.makeText(HomeActivity.this, "出库功能", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HomeActivity.this, OutboundActivity.class);
        startActivity(intent);
      }
    });
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
  @Override
  public void onBackPressed() {
    // 执行退出登录的操作，例如清除凭据
    // clearCredentials();
    Toast.makeText(HomeActivity.this, "已退出登录", Toast.LENGTH_SHORT).show();
    // 返回到登录界面
    Intent intent = new Intent(this, LoginActivity.class);
    startActivity(intent);

    // 结束当前活动
    finish();
  }

}

