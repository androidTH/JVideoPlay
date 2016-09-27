package com.example.min.jvideoplay.bean;

import android.graphics.Color;

import com.example.min.jvideoplay.R;
import com.sherazkhilji.videffect.BlackAndWhiteEffect;
import com.sherazkhilji.videffect.BrightnessEffect;
import com.sherazkhilji.videffect.ContrastEffect;
import com.sherazkhilji.videffect.CrossProcessEffect;
import com.sherazkhilji.videffect.DocumentaryEffect;
import com.sherazkhilji.videffect.DuotoneEffect;
import com.sherazkhilji.videffect.FillLightEffect;
import com.sherazkhilji.videffect.GrainEffect;
import com.sherazkhilji.videffect.GreyScaleEffect;
import com.sherazkhilji.videffect.InvertColorsEffect;
import com.sherazkhilji.videffect.LamoishEffect;
import com.sherazkhilji.videffect.NoEffect;
import com.sherazkhilji.videffect.PosterizeEffect;
import com.sherazkhilji.videffect.SaturationEffect;
import com.sherazkhilji.videffect.SepiaEffect;
import com.sherazkhilji.videffect.SharpnessEffect;
import com.sherazkhilji.videffect.TemperatureEffect;
import com.sherazkhilji.videffect.TintEffect;
import com.sherazkhilji.videffect.VignetteEffect;
import com.sherazkhilji.videffect.interfaces.ShaderInterface;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by xingliao_zgl on 16/7/22.
 */
public class VideoFilterBean {
    private int index;
    private String config;
    private String name;
    private int drawable;
    private ShaderInterface shaderInterface;

    public VideoFilterBean(int index, String name, String config, ShaderInterface shaderInterface, int drawable) {
        this.index = index;
        this.name = name;
        this.config = config;
        this.shaderInterface = shaderInterface;
        this.drawable = drawable;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }

    public ShaderInterface getShaderInterface() {
        return shaderInterface;
    }

    public void setShaderInterface(ShaderInterface shaderInterface) {
        this.shaderInterface = shaderInterface;
    }

    private static final String[] filterNames = {
            "原图",
            "高亮",
            "黑白",
            "冷色",

            "对比度",
            "交叉处理",
            "记录",
            "双色调",
            "补光",

            "纹理",
            "灰度",
            "反色",
            "Lamoish效果",
            "色调分离",

            "饱和",
            "锐度",
            "色温",
            "色调",
            "晕影"

    };

    private static final String[] filterConfigs = {
            "",
            "eq=brightness=0.3",//亮度
            "colorchannelmixer=.3:.4:.3:0:.3:.4:.3:0:.3:.4:.3",//黑白
            "colorchannelmixer=.393:.769:.189:0:.349:.686:.168:0:.272:.534:.131",//褐色效果

            "colorbalance=rs=.3",//颜色平衡 调整rgb得值权重 分为三个阶层 用于调整饱和度和调整颜色偏移值
            "",
            "",
            "",
            "",

            "",
            "",
            "",
            "",
            "",

            "",
            "",
            "",
            "",
            ""
    };

    private static final ShaderInterface[] filterShaders = {
            new NoEffect(),
            new BrightnessEffect(1.6f),
            new BlackAndWhiteEffect(),
            new SepiaEffect(),//褐色,冷色

            new ContrastEffect(0.8f),
            new CrossProcessEffect(),
            new DocumentaryEffect(),
            new DuotoneEffect(Color.RED,Color.GREEN),
            new FillLightEffect(0.8f),

            new GrainEffect(0.8f),
            new GreyScaleEffect(),
            new InvertColorsEffect(),
            new LamoishEffect(),
            new PosterizeEffect(),

            new SaturationEffect(0.5f),
            new SharpnessEffect(0.8f),
            new TemperatureEffect(0.8f),
            new TintEffect(Color.BLUE),
            new VignetteEffect(0.8f)
    };

    public static List<VideoFilterBean> initData() {
        List<VideoFilterBean> data = new ArrayList<>();
        for (int i = 0; i < filterConfigs.length; i++) {
            data.add(new VideoFilterBean(i, filterNames[i], filterConfigs[i], filterShaders[i], R.mipmap.bg_filter));
        }
        return data;
    }
}
