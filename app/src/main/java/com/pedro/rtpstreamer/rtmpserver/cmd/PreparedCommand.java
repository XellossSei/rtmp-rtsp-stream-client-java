package com.pedro.rtpstreamer.rtmpserver.cmd;

/**
 * 预备命令拼接
 */
public class PreparedCommand {
    public String command;
    public String args;

    public PreparedCommand(String command, String args) {
        this.command = command;
        this.args = args;
    }

    public PreparedCommand() {
    }

    public PreparedCommand setArgs(String args) {
        this.args = args;
        return this;
    }

    public PreparedCommand setCommand(String command) {
        this.command = command;
        return this;
    }

    public String getCommand() {
        return command;
    }

    public String getArgs() {
        return args;
    }

}
