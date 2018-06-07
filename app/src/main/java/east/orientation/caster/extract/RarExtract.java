package east.orientation.caster.extract;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.rarfile.FileHeader;

/**
 * Created by ljq on 2018/6/1.
 */

public class RarExtract extends BaseExtract {

    @Override
    public void onExtract(String srcPath, String extractPath, String password, IExtractListener listener) {
        File srcFile = new File(srcPath);
        if (null == extractPath || "".equals(extractPath)) {
            extractPath = srcFile.getParentFile().getPath();
        }
        // 保证文件夹路径最后是"/"或者"\"
        char lastChar = extractPath.charAt(extractPath.length() - 1);
        if (lastChar != '/' && lastChar != '\\') {
            extractPath += File.separator;
        }

        if (listener != null)
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onStartExtract();
                }
            });

        FileOutputStream fileOut = null;
        Archive rarfile = null;

        try {
            rarfile = new Archive(srcFile,password,false);
            FileHeader fh = null;
            final int total = rarfile.getFileHeaders().size();
            for (int i = 0; i < rarfile.getFileHeaders().size(); i++) {
                fh = rarfile.getFileHeaders().get(i);
                String entrypath = "";
                if (fh.isUnicode()) {//解決中文乱码
                    entrypath = fh.getFileNameW().trim();
                } else {
                    entrypath = fh.getFileNameString().trim();
                }
                entrypath = entrypath.replaceAll("\\\\", "/");

                File file = new File(extractPath + entrypath);

                if (fh.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    fileOut = new FileOutputStream(file);
                    rarfile.extractFile(fh, fileOut);
                    fileOut.close();
                }
                if (listener != null) {
                    final int finalI = i;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onExtractProgress(finalI + 1, total);
                        }
                    });
                }
            }
            rarfile.close();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOut != null) {
                try {
                    fileOut.close();
                    fileOut = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (rarfile != null) {
                try {
                    rarfile.close();
                    rarfile = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (listener != null)
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onEndExtract();
                    }
                });
        }
    }
}
