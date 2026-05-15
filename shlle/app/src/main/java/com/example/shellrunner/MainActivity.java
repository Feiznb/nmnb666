package com.example.shellrunner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText etCommand;
    private Button btnExecute;
    private Button btnSelectFile;
    private TextView tvTerminal;

    private String selectedFilePath = "";
    private static final int FILE_SELECT_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etCommand = (EditText) findViewById(R.id.et_command);
        btnExecute = (Button) findViewById(R.id.btn_execute);
        btnSelectFile = (Button) findViewById(R.id.btn_select_file);
        tvTerminal = (TextView) findViewById(R.id.tv_terminal);

        // 执行按钮
        btnExecute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String cmd = etCommand.getText().toString().trim();
                if (cmd.isEmpty()) {
                    tvTerminal.setText("请输入命令或先选择脚本文件");
                    return;
                }
                // 弹出选择对话框
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("执行选项")
                        .setMessage("请选择执行方式：")
                        .setPositiveButton("使用 ROOT 执行", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (RootUtils.isSuAvailable()) {
                                    runCommandWithRoot(cmd);
                                } else {
                                    Toast.makeText(MainActivity.this, "未检测到 su，无法使用 ROOT 执行", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("不使用 ROOT 执行", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                runCommandWithoutRoot(cmd);
                            }
                        })
                        .setNeutralButton("取消", null)
                        .show();
            }
        });

        // 选择文件按钮
        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // 执行按钮弹动效果
        btnExecute.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                }
                return false;
            }
        });

        // 选择文件按钮弹动效果
        btnSelectFile.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                }
                return false;
            }
        });
    }

    // 打开文件选择器
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "选择 sh 脚本"), FILE_SELECT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                selectedFilePath = uri.getPath();
                etCommand.setText("sh \"" + selectedFilePath + "\"");
                tvTerminal.setText("已选择文件：" + selectedFilePath + "\n点击执行即可运行脚本");
            }
        }
    }

    // 使用 ROOT 执行命令
    private void runCommandWithRoot(final String cmd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result;
                    if (cmd.startsWith("sh ")) {
                        String filePath = cmd.substring(3).trim();
                        if (filePath.startsWith("\"") && filePath.endsWith("\"")) {
                            filePath = filePath.substring(1, filePath.length() - 1);
                        }
                        result = RootUtils.execScriptFileWithRoot(filePath);
                    } else {
                        result = RootUtils.execRootCommand(cmd);
                    }
                    final String finalResult = result;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvTerminal.setText("$ [ROOT] " + cmd + "\n" + finalResult);
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvTerminal.setText("执行出错：" + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    // 不使用 ROOT 执行命令
    private void runCommandWithoutRoot(final String cmd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = RootUtils.execNormalCommand(cmd);
                    final String finalResult = result;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvTerminal.setText("$ " + cmd + "\n" + finalResult);
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvTerminal.setText("执行出错：" + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }
}
