/**
 * This class respresent the markers.
 * @author: Unima Diagnosticos.
 * @version: 2018/04/01
 * @since: 1.0.0
 */
public class Marker {

    private float val; //Area or Prob
    private int index;
    private String result;

    public Marker(float val, int index, String result) {
        this.val = val;
        this.index = index;
        this.result = result;
    }

    public float getVal() {
        return val;
    }

    public void setVal(float val) {
        this.val = val;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("Marker: ").append(index)
                .append(", Result: ").append(result)
                .append(", Val: ").append(val);
        return stringBuilder.toString();
    }

}
