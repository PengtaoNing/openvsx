/********************************************************************************
 * Copyright (c) 2020 TypeFox and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.openvsx.json;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;;

@ApiModel(
    value = "UserPublishInfo",
    description = "User publish info"
)
@JsonInclude(Include.NON_NULL)
public class UserPublishInfoJson extends ResultJson {

    public static UserPublishInfoJson error(String message) {
        var userPublishInfo = new UserPublishInfoJson();
        userPublishInfo.error = message;
        return userPublishInfo;
    }

    @ApiModelProperty("The user")
    @NotNull
    public UserJson user;

    @ApiModelProperty("List of extensions published by the user")
    @NotNull
    public List<ExtensionJson> extensions;

    @ApiModelProperty(hidden = true)
    @NotNull
    public List<AccessTokenJson> accessTokens;

}