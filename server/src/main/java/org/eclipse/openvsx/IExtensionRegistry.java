/********************************************************************************
 * Copyright (c) 2019 TypeFox and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.openvsx;

import org.eclipse.openvsx.json.ExtensionJson;
import org.eclipse.openvsx.json.NamespaceJson;
import org.eclipse.openvsx.json.ReviewListJson;
import org.eclipse.openvsx.json.SearchResultJson;

/**
 * Declaration of the registry API methods that can be accessed without authentication.
 */
public interface IExtensionRegistry {

    NamespaceJson getNamespace(String namespace);

    ExtensionJson getExtension(String namespace, String extension);

    ExtensionJson getExtension(String namespace, String extension, String version);

    byte[] getFile(String namespace, String extension, String version, String fileName);

    ReviewListJson getReviews(String namespace, String extension);

    SearchResultJson search(String query, String category, int size, int offset, String sortOrder, String sortBy);

}