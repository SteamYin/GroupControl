package models;

public class FileSystem {
    // M为单位
    public long size;
    public long used;
    public long free;

    public FileSystem(long size, long used, long free){
        this.size = size;
        this.used = used;
        this.free = free;
    }
}
