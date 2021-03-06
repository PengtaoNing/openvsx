/********************************************************************************
 * Copyright (c) 2020 TypeFox and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.openvsx.storage;

import java.net.URI;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.eclipse.openvsx.entities.ExtensionVersion;
import org.eclipse.openvsx.entities.FileResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleCloudStorageService {

    private static final String BASE_URL = "https://storage.googleapis.com/";

    @Autowired
    StorageUtilService storageUtil;

    @Value("${ovsx.storage.gcp.project-id:}")
    String projectId;

    @Value("${ovsx.storage.gcp.bucket-id:}")
    String bucketId;

    private Storage storage;

    public boolean isEnabled() {
        return !Strings.isNullOrEmpty(bucketId);
    }

    protected Storage getStorage() {
        if (storage == null) {
            StorageOptions options;
            if (Strings.isNullOrEmpty(projectId)) {
                options = StorageOptions.getDefaultInstance();
            } else {
                options = StorageOptions.newBuilder()
                        .setProjectId(projectId)
                        .build();
            }
            storage = options.getService();
        }
        return storage;
    }

    @Transactional(TxType.MANDATORY)
    public void uploadFile(FileResource resource) {
        var objectId = getObjectId(resource.getName(), resource.getExtension());
        if (Strings.isNullOrEmpty(bucketId)) {
            throw new IllegalStateException("Cannot upload file "
                    + objectId + ": missing Google bucket id");
        }

        uploadFile(resource.getContent(), resource.getName(), objectId);
        resource.setStorageType(FileResource.STORAGE_GOOGLE);
        // Don't store the binary content in the DB - it's now stored externally
        resource.setContent(null);
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void uploadFileNewTx(FileResource resource) {
        uploadFile(resource);
    }

    protected void uploadFile(byte[] content, String fileName, String objectId) {
        var blobInfoBuilder = BlobInfo.newBuilder(BlobId.of(bucketId, objectId))
                .setContentType(storageUtil.getFileType(fileName).toString());
        if (fileName.endsWith(".vsix")) {
            blobInfoBuilder.setContentDisposition("attachment; filename=\"" + fileName + "\"");
        } else {
            var cacheControl = storageUtil.getCacheControl(fileName);
            blobInfoBuilder.setCacheControl(cacheControl.getHeaderValue());
        }
        getStorage().create(blobInfoBuilder.build(), content);
    }

    public void removeFile(String name, ExtensionVersion extVersion) {
        var objectId = getObjectId(name, extVersion);
        if (Strings.isNullOrEmpty(bucketId)) {
            throw new IllegalStateException("Cannot remove file "
                    + objectId + ": missing Google bucket id");
        }
        getStorage().delete(BlobId.of(bucketId, objectId));
    }

    public URI getLocation(String name, ExtensionVersion extVersion) {
        var objectId = getObjectId(name, extVersion);
        if (Strings.isNullOrEmpty(bucketId)) {
            throw new IllegalStateException("Cannot determine location of file "
                    + objectId + ": missing Google bucket id");
        }
        return URI.create(BASE_URL + bucketId + "/" + objectId);
    }

    protected String getObjectId(String name, ExtensionVersion extVersion) {
        Preconditions.checkNotNull(name);
        var extension = extVersion.getExtension();
        var namespace = extension.getNamespace();
        return namespace.getName()
                + "/" + extension.getName()
                + "/" + extVersion.getVersion()
                + "/" + name;
    }

}