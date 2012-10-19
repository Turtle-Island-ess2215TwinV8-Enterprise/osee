/*
 * Copyright (c) 2012 Robert Bosch Engineering and Business Solutions Ltd India. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.osee.doors.connector.ui.oauth.extension;

import org.eclipse.osee.doors.connector.core.oauth.IDWAOSLCProviderInfo;
import org.eclipse.osee.doors.connector.core.oauth.RequestBaseStringExtractor;
import org.eclipse.osee.doors.connector.core.oauth.RequestHeaderExtractor;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.extractors.RequestTokenExtractor;
import org.scribe.extractors.TokenExtractorImpl;
import org.scribe.services.HMACSha1SignatureService;
import org.scribe.services.SignatureService;
import org.scribe.services.TimestampService;
import org.scribe.services.TimestampServiceImpl;


/**
 * Class to provide the Constants
 * 
 * @author Chandan Bandemutt
 */
public class DoorsOSLCDWAProviderInfoExtn implements IDWAOSLCProviderInfo {

  /**
   * 
   */
  public DoorsOSLCDWAProviderInfoExtn() {
    //
  }

  @Override
  public String getRequestTokenURL() {
    return DoorsOSLCDWAConstants.REQUEST_TOKEN_URL;
  }

  @Override
  public String getAccessTokenURL() {
    return DoorsOSLCDWAConstants.ACCESS_TOKEN_URL;
  }

  @Override
  public String getAuthorizeTokenURL() {
    return DoorsOSLCDWAConstants.AUTHORIZE_URL;
  }

  @Override
  public String getOSLCProviderAuthenticationURL() {
    return DoorsOSLCDWAConstants.AUTHENTICATION_LOGIN_URL;
  }

  @Override
  public AccessTokenExtractor getAccessTokenExtractor() {
    return new TokenExtractorImpl();
  }

  @Override
  public RequestBaseStringExtractor getBaseStringExtractor() {
    return new RequestBaseStringExtractor();
  }

  @Override
  public RequestHeaderExtractor getHeaderExtractor() {
    return new RequestHeaderExtractor();
  }

  @Override
  public RequestTokenExtractor getRequestTokenExtractor() {
    return new TokenExtractorImpl();
  }

  @Override
  public SignatureService getSignatureService() {
    return new HMACSha1SignatureService();
  }

  @Override
  public TimestampService getTimestampService() {
    return new TimestampServiceImpl();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String DWAHostName() {
    return DoorsOSLCDWAConstants.DWA_HOST_NAME;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String ResourceUrl() {
    return DoorsOSLCDWAConstants.PROTECTED_RESOURCE_URL;
  }

}
