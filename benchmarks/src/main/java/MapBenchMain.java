import sun.nio.ch.DirectBuffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.nanoTime;

/*
/tmp max resize: 0.133, map: 1.649, touch: 22.791, unmap: 3.928
nvm max resize: 0.152, map: 6.811, touch: 126.88, unmap: 5.962
hdd max resize: 0.163, map: 1.581, touch: 382.998, unmap: 6.087
 */
public class MapBenchMain {

    // Run with -DchunkSize=64 -DdataSize=40000 (both in MB)

    public static final int CHUNK_SIZE = Integer.getInteger("chunkSize", 64);
    public static final int DATA_SIZE = Integer.getInteger("dataSize", 40 << 10);

    public static void main(String[] args) throws IOException {
        String name = args[0] + "/deleteme-" + nanoTime();
        try (RandomAccessFile raf = new RandomAccessFile(name, "rw")) {
            FileChannel channel = raf.getChannel();
            long size = CHUNK_SIZE << 20;
            List<MappedByteBuffer> buffers = new ArrayList<>();
            double maxResize = 0, maxMap = 0, maxTouch = 0, maxUnmap = 0;
            for (int i = 0; i < DATA_SIZE; i += CHUNK_SIZE) {
                long start = System.nanoTime();
                channel.truncate((i + 1) * size);
                long mid = System.nanoTime();
                MappedByteBuffer mbb = channel.map(FileChannel.MapMode.READ_WRITE, i * size, size);
                buffers.add(mbb);
                long mid2 = System.nanoTime();
                for (int j = 0; j < size; j += 4096)
                    mbb.putLong(j, 0);
                long end = System.nanoTime();
                double resize = (mid - start) / 1000 / 1e3;
                double map = (mid2 - mid) / 1000 / 1e3;
                double touch = (end - mid2) / 1000 / 1e3;
                System.out.println(i + " resize: " + resize
                        + " map: " + map
                        + " touch: " + touch);
                maxResize = Math.max(maxResize, resize);
                maxMap = Math.max(maxMap, map);
                maxTouch = Math.max(maxTouch, touch);
            }
            for (int i = 0; i < buffers.size(); i++) {
                MappedByteBuffer buffer = buffers.get(i);
                long start2 = nanoTime();
                ((DirectBuffer) buffer).cleaner().clean();
                long end2 = nanoTime();
                double unmap = (end2 - start2) / 1000 / 1e3;
                System.out.println(i * CHUNK_SIZE + " unmap: " + unmap);
                maxUnmap = Math.max(maxUnmap, unmap);
            }
            System.out.println("\n" + args[0] + " max resize: " + maxResize + ", map: " + maxMap + ", touch: " + maxTouch + ", unmap: " + maxUnmap);

        } finally {
            new File(name).delete();
        }
    }
}
