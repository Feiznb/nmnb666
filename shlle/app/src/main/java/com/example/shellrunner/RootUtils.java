package com.example.shellrunner;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;

public class RootUtils {

    /**
     * 检测 su 是否可用
     */
    public static boolean isSuAvailable() {
        try {
            Process process = Runtime.getRuntime().exec("su -c id");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            process.waitFor();
            return line != null && line.contains("uid=0");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 执行单条 Root 命令（使用数组方式，避免引号问题）
     */
    public static String execRootCommand(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            return readProcessOutput(process);
        } catch (Exception e) {
            return "执行出错: " + e.getMessage();
        }
    }

    /**
     * 执行单条普通命令（无 Root）
     */
    public static String execNormalCommand(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            return readProcessOutput(process);
        } catch (Exception e) {
            return "执行出错: " + e.getMessage();
        }
    }

    /**
     * 读取进程输出（标准输出 + 错误输出）
     */
    private static String readProcessOutput(Process process) {
        StringBuilder output = new StringBuilder();
        BufferedReader reader = null;
        BufferedReader errorReader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            while ((line = errorReader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();
        } catch (Exception e) {
            output.append("读取输出出错: ").append(e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (errorReader != null) {
                    errorReader.close();
                }
            } catch (Exception ignored) {
            }
        }

        return output.toString();
    }

    /**
     * 用 Root 执行脚本文件（支持直接执行文件路径）
     */
    public static String execScriptFileWithRoot(String filePath) {
        try {
            File scriptFile = new File(filePath);

            // 检查文件是否存在
            if (!scriptFile.exists()) {
                return "错误: 文件不存在: " + filePath;
            }

            // 方法1: 尝试直接用 sh 执行文件（推荐，保留脚本上下文）
            String result = execRootCommand("sh \"" + filePath + "\"");

            // 如果方法1失败（权限或解析问题），使用方法2
            if (result.contains("Permission denied") || result.contains("inaccessible")) {
                // 方法2: 读取内容后通过 su 执行
                result = execScriptByReadingContent(filePath);
            }

            return result;
        } catch (Exception e) {
            return "执行脚本失败: " + e.getMessage();
        }
    }

    /**
     * 通过读取文件内容后执行（备用方案）
     */
    private static String execScriptByReadingContent(String filePath) {
        try {
            // 先用 Root 读取文件内容
            String catResult = execRootCommand("cat \"" + filePath + "\"");

            if (catResult.startsWith("执行出错") || catResult.contains("No such file")) {
                return "读取文件失败: " + catResult;
            }

            // 检查是否为空文件
            if (catResult.trim().isEmpty()) {
                return "错误: 脚本文件为空";
            }

            // 执行脚本内容
            return execScriptContentWithRoot(catResult);
        } catch (Exception e) {
            return "读取并执行失败: " + e.getMessage();
        }
    }

    /**
     * 用 Root 执行脚本内容（将内容写入 su 进程）
     */
    public static String execScriptContentWithRoot(String scriptContent) {
        Process process = null;
        DataOutputStream os = null;
        StringBuilder output = new StringBuilder();
        BufferedReader reader = null;
        BufferedReader errorReader = null;

        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());

            // 写入脚本内容
            os.writeBytes(scriptContent);
            os.writeBytes("\n");
            os.writeBytes("exit\n");
            os.flush();

            // 读取输出
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            while ((line = errorReader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();

        } catch (Exception e) {
            output.append("执行脚本内容出错: ").append(e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (reader != null) {
                    reader.close();
                }
                if (errorReader != null) {
                    errorReader.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception ignored) {
            }
        }

        return output.toString();
    }

    /**
     * 用 Root 执行脚本并设置工作目录（更可靠的方式）
     */
    public static String execScriptWithWorkDir(String filePath) {
        try {
            File scriptFile = new File(filePath);
            String parentDir = scriptFile.getParent();
            String fileName = scriptFile.getName();

            // 构建命令：切换到目录后执行
            String cmd;
            if (parentDir != null && !parentDir.isEmpty()) {
                cmd = "cd \"" + parentDir + "\" && sh \"" + fileName + "\"";
            } else {
                cmd = "sh \"" + filePath + "\"";
            }

            return execRootCommand(cmd);
        } catch (Exception e) {
            return "执行失败: " + e.getMessage();
        }
    }

    /**
     * 复制文件到可执行目录并执行（解决权限问题）
     */
    public static String execScriptWithCopy(String sourcePath, String destDir) {
        try {
            File sourceFile = new File(sourcePath);
            String fileName = sourceFile.getName();

            // 构建目标路径
            String destPath = destDir + "/" + fileName;

            // 复制文件命令
            String copyCmd = "cp \"" + sourcePath + "\" \"" + destPath + "\"";
            String copyResult = execRootCommand(copyCmd);

            // 设置权限
            String chmodCmd = "chmod 755 \"" + destPath + "\"";
            execRootCommand(chmodCmd);

            // 执行
            return execRootCommand("sh \"" + destPath + "\"");
        } catch (Exception e) {
            return "复制并执行失败: " + e.getMessage();
        }
    }
}

