package io.github.yzjdev.utils;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.view.View;
import android.widget.ImageView;

import com.caverock.androidsvg.SVG;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public final class XmlUtils {

    public static final String NS_ANDROID = "http://schemas.android.com/apk/res/android";
    public static final String NS_APP = "http://schemas.android.com/apk/res-auto";
    public static final String NS_TOOLS = "http://schemas.android.com/tools";
    public static final String NS_SVG = "http://www.w3.org/2000/svg";

    private XmlUtils() {
    }

    public static DocumentBuilder createBuilder() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        return factory.newDocumentBuilder();
    }

    public static String toString(Document doc) {
        try {
            // output
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String changeColor(File vdFile, String color) throws Exception {
        DocumentBuilder builder = createBuilder();
        // VectorDrawable document
        Document vdDoc = builder.parse(vdFile);
        Element vdRoot = vdDoc.getDocumentElement();
        vdRoot.setAttribute("android:tint", color);
        // output
        return toString(vdDoc);
    }

    public static String changeAlpha(File vdFile, float alpha) throws Exception {
        DocumentBuilder builder = createBuilder();
        // VectorDrawable document
        Document vdDoc = builder.parse(vdFile);
        Element vdRoot = vdDoc.getDocumentElement();
        vdRoot.setAttribute("android:alpha", alpha + "");
        // output
        return toString(vdDoc);
    }

    public static String change(String vectorString, String w, String h, String color, String alpha) throws Exception {
        return change(new ByteArrayInputStream(vectorString.getBytes()), w, h, color, alpha);
    }

    public static String change(String vectorString, String color, String alpha) throws Exception {
        return change(new ByteArrayInputStream(vectorString.getBytes()), color, alpha);
    }

    public static String change(InputStream in, String color, String alpha) throws Exception {
        DocumentBuilder builder = createBuilder();
        // VectorDrawable document
        Document vdDoc = builder.parse(in);
        Element vdRoot = vdDoc.getDocumentElement();
        vdRoot.setAttribute("android:tint", color);
        vdRoot.setAttribute("android:alpha", alpha);
        return toString(vdDoc);
    }

    public static String change(File in, String w, String h, String color, String alpha) throws Exception {
        return change(new FileInputStream(in), w, h, color, alpha);
    }

    public static String change(InputStream in, String w, String h, String color, String alpha) throws Exception {
        DocumentBuilder builder = createBuilder();
        // VectorDrawable document
        Document vdDoc = builder.parse(in);
        Element vdRoot = vdDoc.getDocumentElement();
        vdRoot.setAttribute("android:width", w + "dp");
        vdRoot.setAttribute("android:height", h + "dp");
        vdRoot.setAttribute("android:tint", color);
        vdRoot.setAttribute("android:alpha", alpha);
        return toString(vdDoc);
    }

    public static String vd2svg(String vectorString) throws Exception {
        return vd2svg(new ByteArrayInputStream(vectorString.getBytes()));
    }

    public static String vd2svg(File vdFile) throws Exception {
        return vd2svg(new FileInputStream(vdFile));
    }

    public static String vd2svg(InputStream vdInputStream) throws Exception {
        DocumentBuilder builder = createBuilder();
        // SVG document
        Document svgDoc = builder.newDocument();
        Element svgRoot = svgDoc.createElement("svg");
        svgRoot.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        svgDoc.appendChild(svgRoot);

        // VectorDrawable document
        Document vdDoc = builder.parse(vdInputStream);
        Element vdRoot = vdDoc.getDocumentElement();

        // width
        if (vdRoot.hasAttribute("android:width")) {
            svgRoot.setAttribute("width", vdRoot.getAttribute("android:width").replace("dp", ""));
        }

        // height
        if (vdRoot.hasAttribute("android:height")) {
            svgRoot.setAttribute("height", vdRoot.getAttribute("android:height").replace("dp", ""));
        }

        // viewBox
        String vw = "";
        String vh = "";
        if (vdRoot.hasAttribute("android:viewportWidth")) {
            vw = vdRoot.getAttribute("android:viewportWidth");
        }
        if (vdRoot.hasAttribute("android:viewportHeight")) {
            vh = vdRoot.getAttribute("android:viewportHeight");
        }
        if (!vw.isEmpty() && !vh.isEmpty()) {
            svgRoot.setAttribute("viewBox", "0 0 " + vw + " " + vh);
        }

        // tint
        boolean hasTint = false;
        if (vdRoot.hasAttribute("android:tint")) {
            hasTint = true;
            var tint = vdRoot.getAttribute("android:tint");
            svgRoot.setAttribute("fill", tint.startsWith("#") ? tint : "currentColor");
        }

        // alpha
        if (vdRoot.hasAttribute("android:alpha")) {
            svgRoot.setAttribute("opacity", vdRoot.getAttribute("android:alpha"));
        }

        // children
        NodeList nodes = vdRoot.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;

            Element el = (Element) node;
            if ("path".equals(el.getTagName())) {
                Element svgPath = svgDoc.createElement("path");

                if (el.hasAttribute("android:pathData")) {
                    svgPath.setAttribute("d", el.getAttribute("android:pathData"));
                }

                if (!hasTint && el.hasAttribute("android:fillColor")) {
                    String color = el.getAttribute("android:fillColor");
                    svgPath.setAttribute("fill", color.startsWith("#") ? color : "currentColor");
                }

                if (el.hasAttribute("android:fillAlpha")) {
                    svgPath.setAttribute("fill-opacity", el.getAttribute("android:fillAlpha"));
                }

                if (el.hasAttribute("android:strokeColor")) {
                    String color = el.getAttribute("android:strokeColor");
                    svgPath.setAttribute("stroke", color.startsWith("#") ? color : "currentColor");
                }

                if (el.hasAttribute("android:strokeAlpha")) {
                    svgPath.setAttribute("stroke-opacity", el.getAttribute("android:strokeAlpha"));
                }

                if (el.hasAttribute("android:strokeWidth")) {
                    svgPath.setAttribute("stroke-width", el.getAttribute("android:strokeWidth"));
                }

                svgRoot.appendChild(svgPath);
            }
        }

        // output
        return toString(svgDoc);
    }

	/* 使用方法
	 String vdPath = "xxx.xml";
	 String svgStr = vd2svg(new File(vdPath));
	 */

    public static void into(ImageView iv, File vectorFile) {
        try {
            SVG svg = SVG.getFromString(vd2svg(vectorFile));
            Drawable d = new PictureDrawable(svg.renderToPicture());
            iv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            iv.setImageDrawable(d);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void into(ImageView iv, String vectorString) {
        try {
            SVG svg = SVG.getFromString(vd2svg(new ByteArrayInputStream(vectorString.getBytes())));
            Drawable d = new PictureDrawable(svg.renderToPicture());
            iv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            iv.setImageDrawable(d);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

