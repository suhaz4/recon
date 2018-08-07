/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oic.hcs.recon.managed;

import java.io.Serializable;
import javax.ejb.EJB;
import javax.faces.event.ActionEvent;
import com.oic.hcs.recon.service.ReconServiceBeanLocal;
import com.oic.hcs.recon.utility.DataAndSummary;

/**
 *
 * @author noaman000
 */
public class ReconBean implements Serializable{

    @EJB
    ReconServiceBeanLocal reconEJB;
    
    private double limit;
    private String userName;
    private String searchForReconType="underProcess";
    private String reconId;   
    DataAndSummary payloadCompleted = new DataAndSummary();
    DataAndSummary payloadUnderProcess = new DataAndSummary();
    DataAndSummary payloadCandidates = new DataAndSummary();
    DataAndSummary payloadOverall = new DataAndSummary();
    DataAndSummary payload = new DataAndSummary();   
    
    public String getSearchForReconType() {
        return searchForReconType;
    }

    public void setSearchForReconType(String searchForReconType) {
        this.searchForReconType = searchForReconType;
    }
    
     public double getLimit() {
        return limit;
    }

    public String getUserName() {
        return userName;
    }
    
    public String getReconId() {
        return reconId;
    }

    public void setReconId(String reconId) {
        this.reconId = reconId;
    }

    public DataAndSummary getPayloadCompleted() {
        return payloadCompleted;
    }

    public void setPayloadCompleted(DataAndSummary payloadCompleted) {
        this.payloadCompleted = payloadCompleted;
    }

    public DataAndSummary getPayloadUnderProcess() {
        return payloadUnderProcess;
    }

    public void setPayloadUnderProcess(DataAndSummary payloadUnderProcess) {
        this.payloadUnderProcess = payloadUnderProcess;
    }

    public DataAndSummary getPayloadCandidates() {
        return payloadCandidates;
    }

    public void setPayloadCandidates(DataAndSummary payloadCandidates) {
        this.payloadCandidates = payloadCandidates;
    }

    public DataAndSummary getPayload() {
        return payload;
    }

    public void setPayload(DataAndSummary payload) {
        this.payload = payload;
    }

    public DataAndSummary getPayloadOverall() {
        return payloadOverall;
    }

    public void setPayloadOverall(DataAndSummary payloadOverall) {
        this.payloadOverall = payloadOverall;
    }
                       
    
    public void searchRecon(ActionEvent e){        
        switch(searchForReconType){
            case "completed":
                payloadCompleted = reconEJB.getCompletedReconByReconId(reconId);                
                payload = payloadCompleted;
                break;
            case "underProcess":
                payloadUnderProcess = reconEJB.getReconClaimsDataByReconId(reconId);                
                payload = payloadUnderProcess;
                break;
            case "candidates":
                payloadCandidates = reconEJB.getCandidatesByReconId(reconId);                
                payload = payloadCandidates;                
                break;
            case "overall":
                payloadOverall = reconEJB.getOverallSummary(reconId);
                payload = payloadOverall;
                break;    
            default: break;    
        }
    }
    
    public void updatePayload(){
        switch(searchForReconType){
            case "completed":                               
                payload = payloadCompleted;
                break;
            case "underProcess":                                
                payload = payloadUnderProcess;
                break;
            case "candidates":                               
                payload = payloadCandidates;                
                break;
            case "overall":                
                payload = payloadOverall;
                break;    
            default: break;    
        }
    }       
     
}
