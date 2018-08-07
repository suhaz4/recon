/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oic.hcs.recon.managed;

import com.oic.hcs.recon.entity.ReconCandidateClaims;
import com.oic.hcs.recon.entity.ReconClaimHdr;
import com.oic.hcs.recon.entity.ReconClaimItems;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import com.oic.hcs.recon.service.ReconServiceBeanLocal;
import com.oic.hcs.recon.util.Util;
import com.oic.hcs.recon.utility.AlreadyExistsException;
import com.oic.hcs.recon.utility.DataAndSummary;

/**
 *
 * @author noaman000
 */
@ManagedBean
@ViewScoped
public class ReviewReconBean implements Serializable{

    @EJB
    ReconServiceBeanLocal reconEJB;
    DataAndSummary payload = new DataAndSummary();
    List<ReconClaimHdr> hdrList;
    List itemsList;
    private List<ReconClaimItems> filteredReconItems;
    private String reconType;
    private String chqEftNum;

    private String reconId;

    public String getReconId() {
        return reconId;
    }

    public void setReconId(String reconId) {
        this.reconId = reconId;
    }

    public DataAndSummary getPayload() {
        return payload;
    }

    public void setPayload(DataAndSummary payload) {
        this.payload = payload;
    }

    public List<ReconClaimHdr> getHdrList() {
        return hdrList;
    }

    public void setHdrList(List<ReconClaimHdr> hdrList) {
        this.hdrList = hdrList;
    }

    public List getItemsList() {
        return itemsList;
    }

    public void setItemsList(List itemsList) {
        this.itemsList = itemsList;
    }

    public List getFilteredReconItems() {
        return filteredReconItems;
    }

    public void setFilteredReconItems(List filteredReconItems) {
        this.filteredReconItems = filteredReconItems;
    }

    public String getReconType() {
        return reconType;
    }

    public void setReconType(String reconType) {
        this.reconType = reconType;
    }

    public String getChqEftNum() {
        return chqEftNum;
    }

    public void setChqEftNum(String chqEftNum) {
        this.chqEftNum = chqEftNum;
    }

    public void searchReconDataForReview() {
        if (reconId != null && !reconId.trim().equals("")) {
            payload = reconEJB.getCandidatesByReconId(reconId);
            List<ReconCandidateClaims> ls = payload.getDataList();
            if (ls != null && ls.size() > 0) {
                reconType = ls.get(0).getReconType();
            }
            if (reconType != null && reconType.equalsIgnoreCase("PN")) {
                payload = reconEJB.getPendingReconClaimsByReconId(reconId);
                itemsList = payload.getDataList();
            } else {
                payload = reconEJB.getReconClaimHdrByReconId(reconId);
                hdrList = payload.getDataList();
                payload = reconEJB.getReconClaimItemsByReconId(reconId);
                itemsList = payload.getDataList();
            }            
        }
    }

    public void updateAllCreditNoteNumber(String reconId) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = Util.getSession();
        String user = (String) session.getAttribute("username");
        try {
            reconEJB.updateCreditNoteNumber(reconId, hdrList, user, chqEftNum);
            searchReconDataForReview();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Information ", "Credit Note No. successfully updated for recon Id " + reconId));
        } catch (AlreadyExistsException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        } catch (Exception ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to update the record. " + ex.getMessage()));
        }
    }

    public void updateCreditNoteNumber(ReconClaimHdr hdr) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = Util.getSession();
        String user = (String) session.getAttribute("username");
        List<ReconClaimHdr> ls = new ArrayList<ReconClaimHdr>();
        ls.add(hdr);
        try {
            reconEJB.updateCreditNoteNumber(hdr.getReconId(), ls, user, hdr.getChqEftNum());
            searchReconDataForReview();
        } catch (AlreadyExistsException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        } catch (Exception ex) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to update the record. " + ex.getMessage()));
        }
    }

    public void acceptClaimsInProduction() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = Util.getSession();
        String user = (String) session.getAttribute("username");
        boolean proceed = true;
        if (reconType.equalsIgnoreCase("AS")) {
            for (ReconClaimHdr hdr : hdrList) {
                if (hdr.getChqEftNum() == null || hdr.getChqEftNum().trim().equals("")) {
                    proceed = false;
                    facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Recon Type - Already Settled Claims: ", "Credit Note No. can't be empty for claim id " + hdr.getCclaimId()));
                    break;
                }
            }
        }
        if (proceed) {
            try {
                reconEJB.acceptClaimsInproduction(reconId, reconType, user);
                searchReconDataForReview();
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Transaction complete. ", "Claims for Recon Id " + reconId + " have been accepted in production."));
            } catch (Exception e) {
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to accept claims in production. " + e.getMessage()));
            }
        }
    }
}
