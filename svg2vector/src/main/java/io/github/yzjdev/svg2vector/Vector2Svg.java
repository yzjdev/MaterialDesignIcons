package io.github.yzjdev.svg2vector;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Convert VectorDrawable to SVG format
 */
public class Vector2Svg {
    private static Logger logger = Logger.getLogger(Vector2Svg.class.getSimpleName());

    public static String parseXmlToSvg(InputStream in, OutputStream out) {
        StringBuilder errorLog = new StringBuilder();
        try {

            var parser = new VdParser();
            var vdTree = parser.parse(in, errorLog);

            if (vdTree != null) {
                Vector2Svg.convertToSvg(vdTree, out);
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errorLog.toString();
    }



    public static String parseXmlToSvg(InputStream in) {
        StringBuilder errorLog = new StringBuilder();
        try {
            var parser = new VdParser();
            var vdTree = parser.parse(in, errorLog);
            if (vdTree != null) {
                return Vector2Svg.parse(vdTree);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errorLog.toString();
    }


    /**
     * Convert a VectorDrawable XML content to SVG format
     *
     * @param vdTree    The parsed VectorDrawable tree
     * @param outStream Output stream to write SVG content
     */
    private static void convertToSvg(VdTree vdTree, OutputStream outStream) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(outStream);

            // Write SVG header
            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            writer.write("<svg xmlns=\"http://www.w3.org/2000/svg\"\n");
            writer.write("     width=\"" + vdTree.getBaseWidth() + "\"\n");
            writer.write("     height=\"" + vdTree.getBaseHeight() + "\"\n");
            writer.write("     viewBox=\"0 0 " + vdTree.mPortWidth + " " + vdTree.mPortHeight + "\">\n");

            // Process all elements in the tree
            writeElements(vdTree.mChildren, writer, "");

            writer.write("</svg>\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String parse(VdTree vdTree) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            convertToSvg(vdTree, baos);
            return baos.toString("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static void writeElements(ArrayList<VdElement> elements, OutputStreamWriter writer, String indent) throws Exception {
        if (elements == null) return;

        for (VdElement element : elements) {
            if (element instanceof VdPath) {
                writePath((VdPath) element, writer, indent + "  ");
            } else if (element instanceof VdGroup) {
                writeGroup((VdGroup) element, writer, indent + "  ");
            }
        }
    }

    private static void writePath(VdPath path, OutputStreamWriter writer, String indent) throws Exception {
        if (path.mNode == null) return;

        writer.write(indent + "<path\n");

        // Write path data
        String pathData = VdPath.Node.NodeListToString(path.mNode);
        writer.write(indent + "     d=\"" + pathData + "\"\n");

        // Write fill color if present
        if (path.mFillColor != 0) {
            String fillColor = formatColor(path.mFillColor);
            writer.write(indent + "     fill=\"" + fillColor + "\"\n");

            // Write fill opacity if not 1.0
            if (!Float.isNaN(path.mFillOpacity) && path.mFillOpacity != 1.0f) {
                writer.write(indent + "     fill-opacity=\"" + path.mFillOpacity + "\"\n");
            }
        } else {
            // Set fill to none if no fill color
            writer.write(indent + "     fill=\"none\"\n");
        }

        // Write stroke color if present
        if (path.mStrokeColor != 0) {
            String strokeColor = formatColor(path.mStrokeColor);
            writer.write(indent + "     stroke=\"" + strokeColor + "\"\n");
            writer.write(indent + "     stroke-width=\"" + path.mStrokeWidth + "\"\n");

            // Write stroke opacity if not 1.0
            if (!Float.isNaN(path.mStrokeOpacity) && path.mStrokeOpacity != 1.0f) {
                writer.write(indent + "     stroke-opacity=\"" + path.mStrokeOpacity + "\"\n");
            }

            // Write stroke linecap if set
            if (path.mStrokeLineCap != -1) {
                String lineCap = getLineCapValue(path.mStrokeLineCap);
                if (lineCap != null) {
                    writer.write(indent + "     stroke-linecap=\"" + lineCap + "\"\n");
                }
            }

            // Write stroke linejoin if set
            if (path.mStrokeLineJoin != -1) {
                String lineJoin = getLineJoinValue(path.mStrokeLineJoin);
                if (lineJoin != null) {
                    writer.write(indent + "     stroke-linejoin=\"" + lineJoin + "\"\n");
                }
            }
        }

        // Close the path tag
        writer.write(indent + "/>\n");
    }

    private static void writeGroup(VdGroup group, OutputStreamWriter writer, String indent) throws Exception {
        writer.write(indent + "<g\n");

        // Write group name if exists
        if (group.mName != null) {
            writer.write(indent + "   id=\"" + group.mName + "\"\n");
        }

        writer.write(indent + ">\n");

        // Write all children of the group
        writeElements(group.getChildren(), writer, indent);

        writer.write(indent + "</g>\n");
    }

    private static String formatColor(int color) {
        // Extract ARGB components
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // If alpha is FF (fully opaque) or NaN, output hex color
        if (a == 0xFF || Float.isNaN(Float.intBitsToFloat(a))) {
            return String.format("#%02X%02X%02X", r, g, b);
        } else {
            // Otherwise, output rgba format
            return String.format("rgba(%d,%d,%d,%f)", r, g, b, a / 255.0f);
        }
    }

    private static String getLineCapValue(int lineCap) {
        switch (lineCap) {
            case 0:
                return "butt";
            case 1:
                return "round";
            case 2:
                return "square";
            default:
                return null;
        }
    }

    private static String getLineJoinValue(int lineJoin) {
        switch (lineJoin) {
            case 0:
                return "miter";
            case 1:
                return "round";
            case 2:
                return "bevel";
            default:
                return null;
        }
    }
}