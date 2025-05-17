package net.openhft.chronicle.core;

public class FindFileMain {
    public static void main(String[] args) {
        System.out.print(OS.findFile("dir1", "dir2", "target").getAbsolutePath());
    }
}
