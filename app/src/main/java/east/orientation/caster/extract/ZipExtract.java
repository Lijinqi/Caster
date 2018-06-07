package east.orientation.caster.extract;

import android.text.TextUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;

/**
 * Created by ljq on 2018/6/1.
 */

public class ZipExtract extends BaseExtract {
    @Override
    public void onExtract(String srcPath, String extractPath, String password, IExtractListener listener) {
        if (TextUtils.isEmpty(srcPath) || TextUtils.isEmpty(extractPath))
            return;
        File src = new File(srcPath);
        if (!src.exists())
            return;
        try {
            ZipFile zFile = new ZipFile(srcPath);
            zFile.setFileNameCharset("GBK");
            if (!zFile.isValidZipFile())
                throw new ZipException("文件不合法!");

            File destDir = new File(extractPath);
            if (destDir.isDirectory() && !destDir.exists()) {
                destDir.mkdir();
            }

            if (zFile.isEncrypted()) {
                zFile.setPassword(password.toCharArray());
            }
            if (listener != null)
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStartExtract();
                    }
                });

            FileHeader fh = null;
            final int total = zFile.getFileHeaders().size();
            for (int i = 0; i < zFile.getFileHeaders().size(); i++) {
                fh = (FileHeader) zFile.getFileHeaders().get(i);
//                String entrypath = "";
//                if (fh.isFileNameUTF8Encoded()) {//解決中文乱码
//                    entrypath = fh.getFileName().trim();
//                } else {
//                    entrypath = fh.getFileName().trim();
//                }
//                entrypath = entrypath.replaceAll("\\\\", "/");
//
//                File file = new File(unrarPath + entrypath);
//                Log.d(TAG, "unrar entry file :" + file.getPath());

                zFile.extractFile(fh,extractPath);

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
        } catch (ZipException e1) {
            e1.printStackTrace();
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
