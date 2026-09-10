package com.bhavin.ai;

public class PageObjectContext {

    private String pageName;
    private String packageName;
    private String availableMethods;

    public PageObjectContext(
            String pageName,
            String packageName,
            String availableMethods) {

        this.pageName = pageName;
        this.packageName = packageName;
        this.availableMethods = availableMethods;
    }

    public String getPageName() {
        return pageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAvailableMethods() {
        return availableMethods;
    }
}