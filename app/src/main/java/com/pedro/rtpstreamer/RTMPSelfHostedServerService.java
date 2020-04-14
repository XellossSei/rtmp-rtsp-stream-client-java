package com.pedro.rtpstreamer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

import com.pedro.rtpstreamer.rtmpserver.bintools.DirectoryCopyTask;
import com.pedro.rtpstreamer.rtmpserver.cmd.CommandBridge;
import com.pedro.rtpstreamer.rtmpserver.cmd.PreparedCommand;

import java.io.File;

public class RTMPSelfHostedServerService extends Service {
    public RTMPSelfHostedServerService() {
    }

    private CommandBridge commandBridge = null;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String externalTargetPath = getTargetPath();

        DirectoryCopyTask directoryCopyTask = new DirectoryCopyTask(this, DirectoryCopyTask.ASSETS_DIR_PREFIX, externalTargetPath);
        directoryCopyTask.setOnCopyFinished((String toDir) -> {
            commandBridge = new CommandBridge();
            String bblliveAbsolutePath = toDir + "/" + DirectoryCopyTask.BIN_FILENAME;
            PreparedCommand bbllive = new PreparedCommand().setCommand(bblliveAbsolutePath);
            commandBridge.pushCommand(bbllive);
            commandBridge.start();
        });
        directoryCopyTask.execute();
        return super.onStartCommand(intent, flags, startId);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, RTMPSelfHostedServerService.class);
        //starter.putExtra();
        context.startService(starter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (commandBridge != null) {
            commandBridge.destroyExec();
            commandBridge.interrupt();
        }

    }

    public String getExternalTargetPath() {
        File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        return externalFilesDir.getAbsolutePath();
    }

    public String getTargetPath() {
        File externalFilesDir = getFilesDir();
        return externalFilesDir.getAbsolutePath();
    }
}
