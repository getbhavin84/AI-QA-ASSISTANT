package com.bhavin.ai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildFailureAnalysis {

    private boolean hasFailure;
    private String summary;
    private String rootCause;
    private List<String> affectedFiles;
    private String recommendedAction;

    public boolean isHasFailure() {
        return hasFailure;
    }

    public void setHasFailure(boolean hasFailure) {
        this.hasFailure = hasFailure;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public List<String> getAffectedFiles() {
        return affectedFiles;
    }

    public void setAffectedFiles(List<String> affectedFiles) {
        this.affectedFiles = affectedFiles;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }
}