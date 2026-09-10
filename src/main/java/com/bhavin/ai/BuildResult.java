
package com.bhavin.ai;

public class BuildResult {

    private boolean success;
    private String output;

    public BuildResult(boolean success, String output) {
        this.success = success;
        this.output = output;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getOutput() {
        return output;
    }
}
