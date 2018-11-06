public class DiskAccessTime {
    float readSingleDimTime; //假设读取单个维度需要0.001s
    float seekSingleTime;

    public DiskAccessTime(float readSingleDimTime,float seekSingleTime){
        this.readSingleDimTime=readSingleDimTime;
        this.seekSingleTime=seekSingleTime;
    }
}
