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

/**
 * Static utility class for exporting to a file.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
public class PartialExportUtil {

    public static FileOutputStream getExportStream(String fileName) throws FileNotFoundException {
        String baseDir = System.getProperty("jboss.server.base.dir");
        File exportDir = new File(baseDir, "export");
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
