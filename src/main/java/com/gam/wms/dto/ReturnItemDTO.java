package com.gam.wms.dto;

public class ReturnItemDTO {
    private String code;
    private String desc;
    private Integer noteQty;
    private String specificReason;
    private Integer damagedQty;
    private String batch;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getNoteQty() {
        return noteQty;
    }

    public void setNoteQty(Integer noteQty) {
        this.noteQty = noteQty;
    }

    public String getSpecificReason() {
        return specificReason;
    }

    public void setSpecificReason(String specificReason) {
        this.specificReason = specificReason;
    }

    public Integer getDamagedQty() {
        return damagedQty;
    }

    public void setDamagedQty(Integer damagedQty) {
        this.damagedQty = damagedQty;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }
}