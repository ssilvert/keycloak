/*
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.keycloak.exportimport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.codehaus.jackson.map.ObjectMapper;
import org.keycloak.exportimport.util.ExportUtils;
import org.keycloak.models.RealmModel;
import org.keycloak.util.JsonSerialization;

/**
 * Static utility class for exporting to a file.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
public class ServerExportUtil {

    public static void serverExport(String representationName,
                                             Object representations,
                                             String fileName,
                                             boolean condensed,
                                             RealmModel realm) throws IOException {
        try (FileOutputStream out = ServerExportUtil.getExportStream(fileName, realm.getName())) {

            ObjectMapper mapper;
            if (condensed) {
                mapper = JsonSerialization.mapper;
            } else {
                mapper = JsonSerialization.prettyMapper;
            }

            ExportUtils.exportToStream(realm, mapper, out, representationName, representations);
        }
    }

    public static FileOutputStream getExportStream(String fileName, String realmName) throws FileNotFoundException {
        if (fileName == null) throw new FileNotFoundException("File name can not be null.");

        String baseDir = System.getProperty("jboss.server.base.dir");
        File exportDir = new File(baseDir, "export");
        if (!exportDir.exists()) exportDir.mkdir();

        exportDir = new File(exportDir, realmName);
        if (!exportDir.exists()) exportDir.mkdir();

        File exportFile = makeUniqueFileName(exportDir, fileName, 0);
        return new FileOutputStream(exportFile);
    }

    public static File makeUniqueFileName(File dir, String fileName, int suffix) {
        File file = new File(dir, fileName + ".json");
        if (!file.exists()) return file;

        file = new File(dir, fileName + "-" + suffix + ".json");
        if (file.exists()) return makeUniqueFileName(dir, fileName, ++suffix);

        return file;
    }
}
