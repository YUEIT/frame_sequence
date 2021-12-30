package cn.yue.base.frame;

import com.bumptech.glide.load.Option;

/**
 * Description :
 * Created by yue on 2021/12/16
 */

public class FrameOption {

    public static Option<Integer> optionDuration() {
        return Option.memory("duration");
    }

    public static Option<Boolean> optionCache() {
        return Option.memory("cache");
    }

}
