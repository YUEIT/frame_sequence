package cn.yue.base.frame.anim;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Description :
 * Created by yue on 2021/12/3
 */

public class FileType {

    /**
     常用文件的文件头如下：(以前六位为准)
     JPEG (jpg)，文件头：FFD8FF
     PNG (png)，文件头：89504E47
     GIF (gif)，文件头：47494638
     TIFF (tif)，文件头：49492A00
     Windows Bitmap (bmp)，文件头：424D
     CAD (dwg)，文件头：41433130
     Adobe Photoshop (psd)，文件头：38425053
     Rich Text Format (rtf)，文件头：7B5C727466
     XML (xml)，文件头：3C3F786D6C
     HTML (html)，文件头：68746D6C3E
     Email [thorough only] (eml)，文件头：44656C69766572792D646174653A
     Outlook Express (dbx)，文件头：CFAD12FEC5FD746F
     Outlook (pst)，文件头：2142444E
     MS Word/Excel (xls.or.doc)，文件头：D0CF11E0
     MS Access (mdb)，文件头：5374616E64617264204A
     WordPerfect (wpd)，文件头：FF575043
     Postscript (eps.or.ps)，文件头：252150532D41646F6265
     Adobe Acrobat (pdf)，文件头：255044462D312E
     Quicken (qdf)，文件头：AC9EBD8F
     Windows Password (pwl)，文件头：E3828596
     ZIP Archive (zip)，文件头：504B0304
     RAR Archive (rar)，文件头：52617221
     Wave (wav)，文件头：57415645
     AVI (avi)，文件头：41564920
     Real Audio (ram)，文件头：2E7261FD
     Real Media (rm)，文件头：2E524D46
     MPEG (mpg)，文件头：000001BA
     MPEG (mpg)，文件头：000001B3
     Quicktime (mov)，文件头：6D6F6F76
     Windows Media (asf)，文件头：3026B2758E66CF11
     MIDI (mid)，文件头：4D546864
     */

    public static boolean isImageFile(InputStream stream) {
        String type = checkType(stream);
        return type.equals("JPEG") || type.equals("PNG");
    }

    public static String checkType(InputStream stream) {
        try {
            stream.mark(3);
            byte[] bytes = new byte[3];
            stream.read(bytes);
            stream.reset();
            return checkType(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String checkType(byte[] src) {
        String str = bytesToHexString(src);
        switch (str) {
            case "FFD8FF": return "JPEG";
            case "89504E": return "PNG";
            case "474946": return "GIF";
            default: return "UNKNOWN";
        }
    }

    private static @NonNull String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length < 3) {
            return "";
        }
        for (int i = 0; i < 3; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }
}
