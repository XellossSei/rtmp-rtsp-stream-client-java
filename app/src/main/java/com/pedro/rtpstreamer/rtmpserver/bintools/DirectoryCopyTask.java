package com.pedro.rtpstreamer.rtmpserver.bintools;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.pedro.rtpstreamer.rtmpserver.cmd.CommandBridge;
import com.pedro.rtpstreamer.rtmpserver.cmd.PreparedCommand;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 目录复制任务
 * <p>
 */
public class DirectoryCopyTask extends AsyncTask<String, Void, String> {
    public Context context;
    List<String> toCopyFileList = new ArrayList<>();
    public static final String TAG = "FileCopy";

    /**
     * 目录复制完毕后回调动作
     */
    public interface OnCopyFinished {
        public void onFinish(String toDir);
    }

    public interface OnStartCopy {
        public void before();
    }

    OnCopyFinished onCopyFinished;
    OnStartCopy onStartCopy;

    public static final String ASSETS_DIR_PREFIX = "file:///android_asset/";

    private final String fromDir;
    private final String toDir;
    public static final String BIN_FILENAME = "bbllive";



    public DirectoryCopyTask(Context context, String fromDir, String toDir) {
        this.context = context;
        this.fromDir = fromDir;
        this.toDir = toDir;

        toCopyFileList.add("/" + BIN_FILENAME);

    }

    public DirectoryCopyTask setOnStartCopy(OnStartCopy onStartCopy) {
        this.onStartCopy = onStartCopy;
        return this;
    }

    public DirectoryCopyTask setOnCopyFinished(OnCopyFinished onCopyFinished) {
        this.onCopyFinished = onCopyFinished;
        return this;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (onStartCopy != null) {
            onStartCopy.before();
        }
    }


    @Override
    protected String doInBackground(String... strings) {
        //为了确保防止可能产生无法执行bin文件问题，或者确保升级后的可执行文件，每次都要执行一遍删除原文件。

        File toDirFile = new File(toDir);
        if (toDirFile.exists()) {
            toDirFile.delete();
        }
        CommandBridge bridge = new CommandBridge();
        InputStream fileFromAssets = null;
        for (int i = 0; i < toCopyFileList.size(); i++) {
            try {
                Log.d(TAG, "正在复制文件：" + fromDir + toCopyFileList.get(i) + "到" + toDir + toCopyFileList.get(i));
                fileFromAssets = FileUtil.getFileFromAssets(context, fromDir + toCopyFileList.get(i));
                FileUtil.copyFileByStream(fileFromAssets, toDir + toCopyFileList.get(i));
                Log.d(TAG, "文件复制完成");
                PreparedCommand preparedCommand = new PreparedCommand();
                preparedCommand.setCommand("chmod").setArgs("755 " + toDir + toCopyFileList.get(i));
                bridge.pushCommand(preparedCommand);
                Log.d(TAG, "文件权限已经设置为755 >>>" + toCopyFileList.get(i));

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "文件复制错误：" + toCopyFileList.get(i));
            }
        }

        bridge.start();

        return toDir;
    }

    @Override
    protected void onPostExecute(String toDir) {
        super.onPostExecute(toDir);
        if (onCopyFinished != null) {
            onCopyFinished.onFinish(toDir);
        }
    }

}
