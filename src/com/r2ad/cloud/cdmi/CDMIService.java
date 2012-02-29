/**
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 Copyright (c) 2010, R2AD, LLC
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of the R2AD, LLC nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.r2ad.cloud.cdmi;

import android.util.Log;

import java.io.InputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;

import com.r2ad.cloud.http.CloudServiceAdapter;
import com.r2ad.cloud.model.CloudStorageType;
import com.r2ad.cloud.model.CloudTypeMap;

public class CDMIService extends CloudServiceAdapter {

	public static final String[] CAPABILITES_HDR = {"Accept", "application/cdmi-capability", "X-CDMI-Specification-Version", "1.0.1"};	
	public static final String[] CONTAINER_HDR = {"Accept", "application/cdmi-container", "X-CDMI-Specification-Version", "1.0.1"};	
	public static final String[] OBJECT_HDR = {"Accept", "application/cdmi-object", "X-CDMI-Specification-Version", "1.0.1"};
		
	private static final String TAG = "CDMIService";
	private static final String TYPE = "CDMI";
	private String[] HEADERS = CAPABILITES_HDR;
	private boolean m_retrieved = false;
	
	// *******************************************************
	// Implement abstract methods from CloudServiceAdapter
	// *******************************************************
	
	@Override
	public boolean authenticate() {	
		HEADERS = CAPABILITES_HDR;
		return getResponse(getURL()) != null;
	}			
		
	@Override
	public void disposeService() {	
		m_retrieved = true;
		CloudTypeMap.clear(this);
	}			
		
	@Override
	public String getType() {
		return TYPE;
	}				
	
	@Override
	public boolean isAvailable() {
		return true;
	}
	
	@Override
	public void refresh() {	
		CloudTypeMap.clear(this);
		m_retrieved = false;
		retrieve();
	}
	
	@Override
	public void retrieve() {
		if (!m_retrieved) {
		    m_retrieved = true;
		    retrieveCapabilities();
		    retrieveChildren();
		}
	}
	
	protected String[] getHeaderInfo() {
		return HEADERS;		
	}
	
	// *******************************************************
	// CDMI Service Specific Methods
	// *******************************************************
	
	private void retrieveCapabilities() {	
		HEADERS = CAPABILITES_HDR;
		String childrenURL = getURL();
		if (childrenURL.endsWith("/")) {
			childrenURL += "cdmi_capabilities";
		} else {
			childrenURL += "/cdmi_capabilities";
		}
		HttpResponse httpResp = getResponse(childrenURL);
		if (httpResp != null) {
		    Log.d(TAG, "Protocol Version: "+ httpResp.getProtocolVersion());
		    Log.d(TAG, "Status Code: " + httpResp.getStatusLine().getStatusCode());
		    Log.d(TAG, "Response Msg: " + httpResp.getStatusLine().getReasonPhrase());
		    Log.d(TAG, "Status: " + httpResp.getStatusLine().toString());				
			String[] children = null;
			try {
			    InputStream stream = httpResp.getEntity().getContent();
			    children = ParseCDMIContainer.parseStorage(stream, childrenURL);
			} catch (IOException oops) {				
			}
			if (children != null) {
                for (int i = 0; i < children.length; i++) {   
                	Log.d(TAG, "Capability " + children[i]);
                	//CloudStorageType temp = new CloudStorageType();
                	//temp.setTitle(children[i]);
                	//CloudTypeMap.add(this, temp);
                }	
			}
		}		
	}	
	
	private void retrieveChildren() {	
		HEADERS = CONTAINER_HDR;
		String childrenURL = getURL();
		if (childrenURL.endsWith("/")) {
			childrenURL += "?children";
		} else {
			childrenURL += "/?children";
		}
		HttpResponse httpResp = getResponse(childrenURL);
		if (httpResp != null) {
		    Log.d(TAG, "Protocol Version: "+ httpResp.getProtocolVersion());
		    Log.d(TAG, "Status Code: " + httpResp.getStatusLine().getStatusCode());
		    Log.d(TAG, "Response Msg: " + httpResp.getStatusLine().getReasonPhrase());
		    Log.d(TAG, "Status: " + httpResp.getStatusLine().toString());				
			String[] children = null;
			try {
			    InputStream stream = httpResp.getEntity().getContent();
			    children = ParseCDMIContainer.parseStorage(stream, childrenURL);
			} catch (IOException oops) {				
			}
			if (children != null) {
                for (int i = 0; i < children.length; i++) {   
                	CloudStorageType temp = new CloudStorageType();
                	temp.setTitle(children[i]);
                	CloudTypeMap.add(this, temp);
                }	
			}
		}		
	}				

}
