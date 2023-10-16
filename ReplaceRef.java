package com.xxx.fucking.algorothm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ReplaceRef {

    public static void main(String[] args) throws IOException {
        final String baseFolder = "D:\\workspace\\github\\alpha-hecoding\\fucking-algorithm";
        final String basePath = new File(baseFolder).getCanonicalPath();
        Pattern refFilePattern = Pattern
                .compile("\\(https://labuladong\\.github\\.io/article/fname\\.html\\?fname=(.*)\\)");
        final String targetIndexPrefix = "(https://labuladong.github.io/article/fname.html?fname=";
        Pattern refImagePattern = Pattern.compile("\\(https://labuladong\\.github\\.io/pictures/.*\\)");
        final String targetImagePrefix = "(https://labuladong.github.io/pictures/";
        final Collection<File> files = listFilesByExtension(new File(baseFolder), new String[] { "md" }, true);
        System.out.println(files.size());
        Map<String, File> map = files.stream()
                .collect(Collectors.toMap(File::getName, Function.identity(), (a, b) -> b));
        map.forEach((k, v) -> {
            try {
                System.out.printf("name: %s, path: %s\n", k, v.getCanonicalPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        for (File file : files) {
            String text = readFileToString(file, StandardCharsets.UTF_8);
            String fromBasePath = "./";
            File parentFile = file.getParentFile();
            while (!parentFile.getCanonicalPath().equals(basePath)) {
                fromBasePath += "../";
                parentFile = parentFile.getParentFile();
            }
            boolean doReplace = false;
            final Matcher refFileMatcher = refFilePattern.matcher(text);
            while (refFileMatcher.find()) {
                final String group = refFileMatcher.group();
                final String mdFileName = group.replace(targetIndexPrefix, "").replace(")", "");
                final File mdFile = map.get(mdFileName + ".md");
                if (mdFile != null) {
                    String resolvePath = fromBasePath + mdFile.getCanonicalPath().replace(basePath, "")
                            .substring(1).replace(File.separator, "/");
                    String replacedGroup = "(" + resolvePath + ")";
                    System.out.println(replacedGroup);
                    text = text.replace(group, replacedGroup);
                    doReplace = true;
                }
            }
            final Matcher refImageMatcher = refImagePattern.matcher(text);
            while (refImageMatcher.find()) {
                final String group = refImageMatcher.group();
                String imagePath = group.replace(targetImagePrefix, "").replace(")", "");
                String resolvePath = fromBasePath + "pictures/" + imagePath;
                if (new File(basePath + "/pictures/" + imagePath).exists()) {
                    String replacedGroup = "(" + resolvePath + ")";
                    System.out.println(replacedGroup);
                    text = text.replace(group, replacedGroup);
                    doReplace = true;
                }
            }
            if (doReplace) {
                writeByteArrayToFile(file, text.getBytes(StandardCharsets.UTF_8), false);
            }
        }
    }

    /**
     * 列举目录下符合扩展名的文件
     * 
     * @param directory  目录
     * @param extensions 文件扩展名
     * @param recursive  是否递归
     * @return 文件列表
     * @throws IOException
     */
    public static List<File> listFilesByExtension(final File directory, final String[] extensions,
            final boolean recursive) throws IOException {
        return Files.walk(directory.toPath(), recursive ? Integer.MAX_VALUE : -1, FileVisitOption.FOLLOW_LINKS)
                .filter(path -> {
                    if (!path.toFile().isFile()) {
                        return false;
                    }
                    if (extensions == null || extensions.length == 0) {
                        return true;
                    }
                    for (int i = 0; i < extensions.length; i++) {
                        if (path.toFile().getName().endsWith(extensions[i])) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(Path::toFile).collect(Collectors.toList());
    }

    /**
     * 读取文件到文本
     * 
     * @param file    文件
     * @param charset 字符编码
     * @return 文本
     * @throws IOException
     */
    public static String readFileToString(final File file, final Charset charset) throws IOException {
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            return new String(readInputStream(inputStream), charset);
        }
    }

    /**
     * 读取输入流数据到字节数组
     * 
     * @param inputStream 输入流
     * @return 字节数组
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            // 创建一个Buffer字符串
            byte[] buffer = new byte[4096];
            // 每次读取的字符串长度，如果为-1，代表全部读取完毕
            int len;
            // 使用一个输入流从buffer里把数据读取出来
            while ((len = inputStream.read(buffer)) != -1) {
                // 用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
                outStream.write(buffer, 0, len);
            }
            // 把outStream里的数据写入内存
            return outStream.toByteArray();
        }
    }

    /**
     * 写字节数组到文件
     * 
     * @param file   文件
     * @param data   内容(字节数组)
     * @param append 是否扩展
     * @throws IOException
     */
    public static void writeByteArrayToFile(final File file, final byte[] data, final boolean append)
            throws IOException {
        try (OutputStream out = new FileOutputStream(file, append)) {
            out.write(data, 0, data.length);
        }
    }

}
