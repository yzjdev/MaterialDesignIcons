/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.yzjdev.svg2vector;

//import com.android.ide.common.blame.SourcePosition;
//import com.android.utils.PositionXmlParser;
//import com.google.common.base.Strings;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Represent the SVG file in an internal data structure as a tree.
 */
class SvgTree {
    private static Logger logger = Logger.getLogger(SvgTree.class.getSimpleName());

    public float w;
    public float h;
    public float[] matrix;
    public float[] viewBox;
    public float mScaleFactor = 1;

    private SvgGroupNode mRoot;
    private String mFileName;

    private ArrayList<String> mErrorLines = new ArrayList<String>();

    public enum SvgLogLevel {
        ERROR,
        WARNING
    }



    public Document parse(File file) throws Exception {
        mFileName = file.getName();
        if (!file.exists() || file.length() == 0) {
            throw new IOException("文件不存在或为空: " + file.getAbsolutePath());
        }

        String xmlContent = readFileToString(file);
        if (xmlContent.trim().isEmpty()) {
            throw new IOException("读取的文件内容为空");
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (ParserConfigurationException e) {
            // Android不支持该特性，忽略
        }

        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xmlContent));
        return builder.parse(inputSource);
    }

    private String readFileToString(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toString("UTF-8"); // 根据文件编码修改
        } finally {
            fis.close();
        }
    }


//    public Document parse(File f) throws Exception {
//        mFileName = f.getName();
//        Document doc = PositionXmlParser.parse(new FileInputStream(f), false);
//        return doc;
//    }

    public void normalize() {
        if (matrix != null) {
            transform(matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
        }

        if (viewBox != null && (viewBox[0] != 0 || viewBox[1] != 0)) {
            transform(1, 0, 0, 1, -viewBox[0], -viewBox[1]);
        }
        logger.log(Level.FINE, "matrix=" + Arrays.toString(matrix));
    }

    private void transform(float a, float b, float c, float d, float e, float f) {
        mRoot.transform(a, b, c, d, e, f);
    }

    public void dump(SvgGroupNode root) {
        logger.log(Level.FINE, "current file is :" + mFileName);
        root.dumpNode("");
    }

    public void setRoot(SvgGroupNode root) {
        mRoot = root;
    }


    public SvgGroupNode getRoot() {
        return mRoot;
    }

//    public void logErrorLine(String s, Node node, SvgLogLevel level) {
//        if (!Strings.isNullOrEmpty(s)) {
//            if (node != null) {
//                SourcePosition position = getPosition(node);
//                mErrorLines.add(level.name() + "@ line " + (position.getStartLine() + 1) +
//                                " " + s + "\n");
//            } else {
//                mErrorLines.add(s);
//            }
//        }
//    }

    /**
     * @return Error log. Empty string if there are no errors.
     */
//    @NonNull
//    public String getErrorLog() {
//        StringBuilder errorBuilder = new StringBuilder();
//        if (!mErrorLines.isEmpty()) {
//            errorBuilder.append("In " + mFileName + ":\n");
//        }
//        for (String log : mErrorLines) {
//            errorBuilder.append(log);
//        }
//        return errorBuilder.toString();
//    }

    /**
     * @return true when there is no error found when parsing the SVG file.
     */
    public boolean canConvertToVectorDrawable() {
        return mErrorLines.isEmpty();
    }

//    private SourcePosition getPosition(Node node) {
//        return PositionXmlParser.getPosition(node);
//    }

}
