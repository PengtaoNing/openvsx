/********************************************************************************
 * Copyright (c) 2020 TypeFox and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.openvsx.security;

import org.eclipse.openvsx.UserService;
import org.eclipse.openvsx.repositories.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Service
public class ExtendedOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    RepositoryService repositories;

    @Autowired
    UserService users;

    @Autowired
    EntityManager entityManager;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oauth2UserRequest, OAuth2User oauth2User) {
        String registrationId = oauth2UserRequest.getClientRegistration().getRegistrationId();
        String loginName = "github".equals(registrationId) ? oauth2User.getAttribute("login") : oauth2User.getAttribute("github_handle");
        var userData = repositories.findUserByLoginName("github", loginName);
        if (userData != null) {
            userData = users.updateExistingUser(userData, oauth2User);
        } else {
            userData = users.registerNewUser(oauth2User, oauth2UserRequest.getClientRegistration());
        }
        return oauth2User;
    }

}