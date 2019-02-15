package com.tripin.directory.ilncontrol.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ReportedCompanyPojo {

    private String mCompanyName;
    private String mCompanyUserName;

    private String mCompanyRmn;
    private String mCompanyUid;
    private String mCompanyFuid;


    private Double mReportsCount;
    private Double mReportStatus;

    @ServerTimestamp
    private Date mTimeStamp;

    public ReportedCompanyPojo(String mCompanyName, String mCompanyUserName, String mCompanyRmn, String mCompanyUid, String mCompanyFuid, Double mReportsCount, Double mReportStatus) {
        this.mCompanyName = mCompanyName;
        this.mCompanyUserName = mCompanyUserName;
        this.mCompanyRmn = mCompanyRmn;
        this.mCompanyUid = mCompanyUid;
        this.mCompanyFuid = mCompanyFuid;
        this.mReportsCount = mReportsCount;
        this.mReportStatus = mReportStatus;
    }

    public ReportedCompanyPojo() {
    }

    public String getmCompanyName() {
        return mCompanyName;
    }

    public void setmCompanyName(String mCompanyName) {
        this.mCompanyName = mCompanyName;
    }

    public String getmCompanyUserName() {
        return mCompanyUserName;
    }

    public void setmCompanyUserName(String mCompanyUserName) {
        this.mCompanyUserName = mCompanyUserName;
    }

    public String getmCompanyRmn() {
        return mCompanyRmn;
    }

    public void setmCompanyRmn(String mCompanyRmn) {
        this.mCompanyRmn = mCompanyRmn;
    }

    public String getmCompanyUid() {
        return mCompanyUid;
    }

    public void setmCompanyUid(String mCompanyUid) {
        this.mCompanyUid = mCompanyUid;
    }

    public String getmCompanyFuid() {
        return mCompanyFuid;
    }

    public void setmCompanyFuid(String mCompanyFuid) {
        this.mCompanyFuid = mCompanyFuid;
    }

    public Double getmReportsCount() {
        return mReportsCount;
    }

    public void setmReportsCount(Double mReportsCount) {
        this.mReportsCount = mReportsCount;
    }

    public Double getmReportStatus() {
        return mReportStatus;
    }

    public void setmReportStatus(Double mReportStatus) {
        this.mReportStatus = mReportStatus;
    }

    public Date getmTimeStamp() {
        return mTimeStamp;
    }

    public void setmTimeStamp(Date mTimeStamp) {
        this.mTimeStamp = mTimeStamp;
    }
}
