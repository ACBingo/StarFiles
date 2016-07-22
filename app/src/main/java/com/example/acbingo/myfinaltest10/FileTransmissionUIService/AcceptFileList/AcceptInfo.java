package com.example.acbingo.myfinaltest10.FileTransmissionUIService.AcceptFileList;

/*
   Created by zhaoshuai on 2016/5/26.
   用于更新发送文件列表 Listview 组件时使用的组件更新信息的载体类
 */
public class AcceptInfo {
    public static  String STATUS_PENDING = "等待中...";
    public static  String STATUS_START   =  "开始传输";
    public static  String STATUS_COMPLETE = "传输完成";
    public static  String STATUS_REFUSED =  "用户未选择";
    private int page_id;
    private int  id;
    private String name;
    private String size;
    private String downloadPercent;
    public AcceptInfo(){
          id = 0;
          downloadPercent = STATUS_PENDING;
          size = "";
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDownloadPercent() {
        return downloadPercent;
    }
    public String getSize(){return size;}
    public void setDownloadPercent(String downloadPercent) {
        this.downloadPercent = downloadPercent;
    }
    public int getPage_id() {
        return page_id;
    }

    public void setSize(String x) { size = x;}
    public void setPage_id(int page_id) {
        this.page_id = page_id;
    }
    public void setStatusComplete(){
          this.downloadPercent = STATUS_COMPLETE;
    }
    public void setStatusStart(){
        this.downloadPercent = STATUS_START;
    }
    public void setStatusPending(){
        this.downloadPercent = STATUS_PENDING;
    }
    public void setStatusRefused(){
        this.downloadPercent = STATUS_REFUSED;
    }
    public boolean isNotinTransformationStatus(){
           if(downloadPercent == STATUS_PENDING) return false;
           if(downloadPercent == STATUS_START) return false;
           if(downloadPercent == STATUS_COMPLETE) return false;
           if(downloadPercent == STATUS_REFUSED) return false;
           return true;
    }
}
