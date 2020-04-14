package com.pedro.rtpstreamer.rtmpserver.bintools;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 二进制文件复制和执行工具
 */
public abstract class BinFileHelper extends AsyncTask<Void, Void, Void> {
    protected Context context;
    private File targetFile;
    /**
     * 目标文件所在目录和文件名
     */
    public static String BIN_FILE_PARENT = "";
    public static final String BIN_FILENAME = "bbllive";

    public BinFileHelper(Context context) {
        this.context = context;
        BIN_FILE_PARENT = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath();

        targetFile = new File(BIN_FILE_PARENT, BIN_FILENAME);
    }

    /**
     * 二进制文件是否存在
     *
     * @return
     */
    public boolean targetFileExist() {
        return targetFile.exists();
    }

    public Context getContext() {
        return context;
    }

    /**
     * 开始复制二进制文件并执行
     */
    public void startCopyBinFile() {
        execute();
    }

    public interface OnFinish {
        void onFinish(File targetFile);
    }

    OnFinish onFinish;

    public void setOnFinish(OnFinish onFinish) {
        this.onFinish = onFinish;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (!targetFileExist()) {
            try {
                InputStream fileFromAssets = FileUtil.getFileFromAssets(context, BIN_FILENAME);
                FileUtil.copyFileByStream(fileFromAssets, targetFile.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public abstract void copyFinished(File targetFile);

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        copyFinished(targetFile);
        if (onFinish != null) {
            onFinish.onFinish(targetFile);
        }
        Log.d("BIN", "文件复制已完成: " + targetFile.getAbsolutePath()
                + " 存在?" + targetFileExist());
    }


    /**
     * 默认文件执行工具
     */
    public static class DefaultBinFileHelper extends BinFileHelper {
        public DefaultBinFileHelper(Context context) {
            super(context);
        }

        @Override
        public void copyFinished(File targetFile) {

        }
    }
}
