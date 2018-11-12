package ParaStructure.Sample;

public class Sample {
    public boolean click;
    public float feature[];
    public int cat[];
    public Sample(float[] feature,int[] cat, boolean click){
        this.click=click;
        this.feature=feature;
        this.cat=cat;
    }

    public Sample(int[] cat){
        this.cat=cat;
    }

}

