package ur_rna.Utilities;

import ur_rna.Utilities.annotation.NotNull;
import ur_rna.Utilities.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class PathTools {
    private PathTools() {} //cannot instantiate.

    /**
     * Gets the base name of the file -- i.e. the name without extension.
     * If the file name contains multiple dot (.) characters, the extension is defined as the segment starting with
     * the <em>final</em> dot.
     *
     * @param fullName The full file name or path.
     * @return The base name of the file -- i.e. the name without extension.
     * @see #getBaseName(String, boolean)
     */
    public static String getBaseName(String fullName) { return getBaseName(fullName, false); }

    /**
     * Gets the base name of the file -- i.e. the name without extension.
     *
     * @param fullName                The full file name or path.
     * @param allowMultiDotExtensions If true, the extension starts at the <em>first</em> dot (.) encountered, which
     *                                means the resulting base name will never contain any dots.
     *                                If false, the extension starts at the <em>last</em> dot, so the base name will
     *                                contain all dots except the final one.
     * @return The base name of the file -- i.e. the name without any extension.
     */
    public static String getBaseName(String fullName, boolean allowMultiDotExtensions) { return getParts(fullName, false, allowMultiDotExtensions).baseName; }

    /**
     * Gets the extension of the file <em>including</em> the preceding dot (.)
     * If the file name contains multiple dot (.) characters, the extension is defined as the segment starting with
     * the <em>final</em> dot.
     *
     * @param fullName The full file name or path.
     * @return The extension of the file <em>including</em> the preceding dot (.)
     */
    public static String getExt(String fullName) { return getExt(fullName, false); }

    /**
     * Gets the extension of the file <em>including</em> the preceding dot (.)
     *
     * @param fullName                The full file name or path.
     * @param allowMultiDotExtensions If true, the extension starts at the <em>first</em> dot (.) encountered in
     *                                the file name, which means the resulting extension may contain more than one dot.
     *                                If false, the extension starts at the <em>last</em> dot, so the extension will
     *                                either be empty (if the file has no extension) or it will contain exactly one
     *                                dot as its first character.
     * @return The extension of the file <em>including</em> the preceding dot (.)
     */
    public static String getExt(String fullName, boolean allowMultiDotExtensions) { return getParts(fullName, false, allowMultiDotExtensions).ext; }

    /**
     * Get the full parent directory path, including the final slash.
     *  <table>
     *  <caption>Examples:</caption>
     *  <tr><td>  /path/to/fileName.ext </td><td>--&gt;</td><td>  /path/to/     </td></tr>
     *  <tr><td>  /fileName.ext         </td><td>--&gt;</td><td>  /             </td></tr>
     *  <tr><td>  C:\Windows\System32   </td><td>--&gt;</td><td>  C:\Windows\   </td></tr>
     *  <tr><td>  images\banana.jpg     </td><td>--&gt;</td><td>  images\       </td></tr>
     *  <tr><td>  HelloWorld.txt        </td><td>--&gt;</td><td>  (empty)       </td></tr>
     *  </table>
     * @param fullName The full path to the file.
     * @return The full parent directory path, including the final slash, or an empty string if the full
     * path does not contain a directory separator character {@code \} or {@code /}.
     */
    public static String getPath(String fullName) { return getParts(fullName, false).path; }

    public static PathParts getParts(String fullFilePath) { return getParts(fullFilePath, true); }
    public static PathParts getParts(String fullFilePath, boolean splitDirs) { return new PathParts(fullFilePath, splitDirs); }
    public static PathParts getParts(String fullFilePath, boolean splitDirs, boolean allowMultiDotExtensions) { return new PathParts(fullFilePath, splitDirs, allowMultiDotExtensions); }
    /**
     * Gets the absolute path to the directory containing the class file for referenceClass.
     * For a jar'd application, this is the directory containing the Jar file in which referenceClass resides.
     * @return The absolute path to the directory containing the referenceClass. The function returns {@code null}
     * if security restrictions do not permit resolution of paths.
     */
    public static @Nullable String getAppPath(Class referenceClass) {
        try {
            URL jarPath = referenceClass.getProtectionDomain().getCodeSource().getLocation();
            File jarFile = new File(jarPath.toURI());
            return jarFile.getParent();
        } catch (Exception ex) {
            return null;
        }
    }

    public static class PathParts {
        private static String[] EMPTY_ARRAY = new String[0];
        private static String[] SINGLE_EMPTY_ITEM_ARRAY = new String[]{""};
        public String path;
        public String name;
        public String baseName;
        public String ext;
        public String[] dirs;
        public PathParts(String fullPath) { this(fullPath, true, false); }
        public PathParts(String fullPath, boolean splitDirs)  { this(fullPath, splitDirs, false); }
        public PathParts(String fullPath, boolean splitDirs, boolean allowMultiDotExtensions) {
            int pos = Math.max(fullPath.lastIndexOf('/'), fullPath.lastIndexOf('\\'));
            if (pos == -1) {
                name = fullPath;
                path = "";
            } else {
                //    /path/to/fileName.ext -->     path=/path/to/      name=fileName.ext
                //    /fileName.ext         -->     path=/              name=fileName.ext
                path = fullPath.substring(0, pos + 1); //keep ending slash if present, to distinguish "" from "/"
                name = fullPath.substring(path.length());
            }

            if (splitDirs) {
                //   "/path/to/"  --> [ "", "path", "to" ]
                //   "/"          --> [ "" ]
                dirs = path.isEmpty() ? EMPTY_ARRAY :
                        path.equals("/") ? SINGLE_EMPTY_ITEM_ARRAY :
                                path.substring(0, path.length() - 1).split("[/\\\\]]");
            } else
                dirs = null;

            pos = allowMultiDotExtensions ? name.indexOf('.') : name.lastIndexOf('.');
            if (pos == -1) {
                baseName = name;
                ext = "";
            } else {
                baseName = name.substring(0, pos);
                ext = name.substring(pos, name.length()); // keep the preceding dot
            }
        }
    }

    public static String addSlash(String path) {
        // add a slash if it is not already there.
        if ((path.endsWith("/") || path.endsWith("\\") || path.endsWith(Character.toString(File.separatorChar))))
            return path;
        return path + File.separatorChar;
    }

    /**
     * Get the canonical form of a file-system path by:
     * <ol>
     *     <li>Converting it to unix format (if Windows)</li>
     *     <li>Replacing {@code //} with {@code /}</li>
     *     <li>Replacing {@code /./} with {@code /}</li>
     *     <li>Replacing {@code dirname/../} with {@code /}</li>
     * </ol>
     * @param path The path to canonicalize
     * @return A canonical unix-style form of the path
     * @see #getCanonicalPath(String, boolean, boolean, boolean, boolean)
     */
    public static String getCanonicalPath(String path) {
        return getCanonicalPath(path, false, false, false, false);
    }

    /**
     * Get the canonical form of a file-system path by:
     * <ol>
     *     <li>Converting it to unix format (if Windows)</li>
     *     <li>Removing starting slashes (if {@code removeStartSlash} is true)</li>
     *     <li>Adding an ending slash (if {@code addEndSlash} is true}</li>
     *     <li>Removing an ending slash (if {@code removeEndSlash} is true</li>
     *     <li>Replacing {@code //} with {@code /}</li>
     *     <li>Replacing {@code /./} with {@code /}</li>
     *     <li>Replacing {@code dirname/../} with {@code /}</li>
     *     <li>Removing {@code ./} at the start of a path (if {@code removeDotSlashAtStart} is true)</li>
     * </ol>
     * @param path The path to canonicalize
     * @param addEndSlash If true, any slashes at the end of the path will be removed. i.e.  {@code path/}  becomes  {@code path}
     * @param removeEndSlash If true any path not ending with a slash will have one appended. i.e.  {@code path}  becomes  {@code path/}
     * @param removeStartSlash If true, any slashes at the beginning of the path will be removed. i.e.  {@code /path}  becomes  {@code path}
     * @param removeDotSlashAtStart If this is true and the path starts with {@code ./}, it will be removed.  i.e.  {@code ./path}  becomes  {@code path}
     * @return A canonical unix-style form of the path
     */
    public static String getCanonicalPath(String path, boolean addEndSlash, boolean removeEndSlash, boolean removeStartSlash, boolean removeDotSlashAtStart) {
        if (path == null) return null;
        if (path.length() == 0) return path;

        path = path.replace("\\", "/");

        // TODO: Is "/" considered an starting slash or ending slash? I.e. should it be removed if removeStartSlash is true? Should it be removed if removeEndSlash is true?

        if (removeStartSlash) {
            // Remove starting slash
            while (path.startsWith("/"))
                path = path.substring(1);
        }

        if (addEndSlash && !path.endsWith("/"))
            path += "/";

        if (removeEndSlash && path.endsWith("/") && path.length() > 1)
            path = path.substring(0, path.length()-1);

        String oldPath;
        Pattern updir = Pattern.compile("([^/]?[^/.][^/])?/\\.\\./");  // replace dirname/../ with just /
        Pattern updirEnd = Pattern.compile("([^/]?[^/.][^/])?/\\.\\.$");  // replace dirname/.. (at the end of a path) with just /
        do {
            oldPath = path;
            path = path.replace("//", "/");
            path = path.replace("/./", "/");
            path = updir.matcher(path).replaceAll("/");
            path = updirEnd.matcher(path).replaceAll("");
        } while (!oldPath.equals(path));

        if (removeDotSlashAtStart && path.startsWith("./"))
            path = path.substring(2);

        return path;
    }

    //"*.{java,class,jar}"
    public static List<File> listFiles(String dirPath, @NotNull String filter) {
        Path dir = Paths.get(dirPath);
        List<File> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, filter)) {
            for (Path entry : stream) {
                files.add(entry.toFile());
            }
            return files;
        } catch (IOException ex) {
            throw new RuntimeException(String.format("error reading folder %s: %s",
                    dir,
                    ex.getMessage()),
                    ex);
        }
    }
}
