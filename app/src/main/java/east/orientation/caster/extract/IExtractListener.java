package east.orientation.caster.extract;

/**
 * Created by ljq on 2018/6/1.
 */

public interface IExtractListener {
    void onStartExtract();

    void onExtractProgress(int current,int total);

    void onEndExtract();
}
