package com.pedro.rtpstreamer.rtmpserver.cmd;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

/**
 * 命令执行桥
 */
public class CommandBridge extends Thread {
    private static final int START = 0;
    private static final int RUNNING = 1;
    private static final int STAND_STILL = 2;

    public interface OnStatusChangeEvent {
        public void onStatusChange(int status);
    }

    int pipeStatus;
    OnStatusChangeEvent onStatusChangeEvent = new OnStatusChangeEvent() {
        @Override
        public void onStatusChange(int status) {

        }
    };

    public CommandBridge setOnStatusChangeEvent(OnStatusChangeEvent onStatusChangeEvent) {
        this.onStatusChangeEvent = onStatusChangeEvent;
        return this;
    }

    public static final String TAG = "CMD";
    String proxyCommand = null;
    LinkedList<String> commands = new LinkedList<>();
    Process exec;

    Thread inStreamThread = null;
    Thread errStreamThread = null;

    InputStreamHelper helperInput = new InputStreamHelper();
    InputStreamHelper helperError = new InputStreamHelper();

    public CommandBridge() {
    }


    public CommandBridge pushCommand(PreparedCommand preparedCommand) {
        String command = preparedCommand.getCommand() + " " + preparedCommand.getArgs();
        commands.add(command);
        return this;
    }

    public void run() {
        super.run();
        for (int i = 0; i < commands.size(); i++) {
            runCommand(commands.get(i));
        }
        commands.clear();

    }

    public void runCommand(final String command) {
        try {
            Log.d("CMD", "$" + command);
            exec = Runtime.getRuntime().exec(command);
            if (onStatusChangeEvent != null) {
                onStatusChangeEvent.onStatusChange(pipeStatus = START);
            }
            inStreamThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream inputStream = exec.getInputStream();
                    try {
                        helperInput.printInputStream("stdout", inputStream);
                        if (onStatusChangeEvent != null) {
                            onStatusChangeEvent.onStatusChange(pipeStatus = RUNNING);
                        }
                    } catch (IOException e) {
                    } finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        helperInput.closeStream();
                    }
                }
            });
            errStreamThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream errorStream = exec.getErrorStream();
                    try {
                        helperError.printInputStream("stderr", errorStream);
                    } catch (IOException e) {
                    } finally {
                        try {
                            errorStream.close();
                        } catch (IOException e) {
                        }
                        helperError.closeStream();
                    }
                }
            });
            inStreamThread.start();
            errStreamThread.start();
            exec.waitFor();
            Log.d(TAG, "Subprocess exited return: " + exec.exitValue());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (onStatusChangeEvent != null) {
                onStatusChangeEvent.onStatusChange(pipeStatus = STAND_STILL);
            }
        }
    }


    public CommandBridge destroyExec() {
        if (exec != null) {
            helperInput.closeStream();
            helperError.closeStream();
            exec.destroy();
        }
        if (onStatusChangeEvent != null) {
            onStatusChangeEvent.onStatusChange(pipeStatus = STAND_STILL);
        }
        return this;
    }
}
