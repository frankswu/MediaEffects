package com.example.android.mediaeffects;


import android.opengl.GLES10;
import android.opengl.GLES30;
import android.util.Log;

import hugo.weaving.DebugLog;

public class GLES3Utils {


    private static final String TAG = "GLES3Utils";

    @DebugLog
    public static int loadShader(int shaderType, String source) {
        // Shaders是一段GLSL小程序，运行在GPU上而非CPU
        int shader = GLES30.glCreateShader(shaderType);
        if (shader != 0) {
            // 下一步我们把这个着色器源码附加到着色器对象上，然后编译它：
            //glShaderSource函数把要编译的着色器对象作为第一个参数。第二参数参数是顶点着色器真正的源码
            GLES30.glShaderSource(shader, source);
            GLES30.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                String info = GLES30.glGetShaderInfoLog(shader);
                GLES30.glDeleteShader(shader);
                throw new RuntimeException("Could not compile shader " + shaderType + ":" + info);
            } else {
                Log.i(TAG, "create shader 3 is success:" + shader);
            }
        } else {
            Log.i(TAG, "load shader 3 is error");
        }
        return shader;
    }

    @DebugLog
    public static int createProgram(String vertexSource, String fragmentSource) {
        // Vertex shader主要用来将点（x，y，z坐标）变换成不同的点。
        int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource);
        // Fragment shader的主要功能是计算每个需要绘制的像素点的颜色。
        int pixelShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource);
        //两个着色器现在都编译了，剩下的事情是把两个着色器对象链接到一个用来渲染的着色器程序(Shader Program)中。
        // 着色器程序对象(Shader Program Object)是多个着色器合并之后并最终链接完成的版本。如果要使用刚才编译的着色器我们必须把它们链接为一个着色器程序对象，然后在渲染对象的时候激活这个着色器程序。已激活着色器程序的着色器将在我们发送渲染调用的时候被使用。
        int program = GLES30.glCreateProgram();
        if (program != 0) {
            GLES30.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES30.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES30.glLinkProgram(program);
            // 就像着色器的编译一样，我们也可以检测链接着色器程序是否失败，并获取相应的日志。现在我们使用：
            int[] linkStatus = new int[1];
            GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES30.GL_TRUE) {
                String info = GLES30.glGetProgramInfoLog(program);
                GLES30.glDeleteProgram(program);
                String message = "Could not link program: " + info;
                Log.i(TAG, message);
                throw new RuntimeException(message);
            } else {
                Log.i(TAG, "create program is success:" + program);
            }
        } else {
            Log.i(TAG, "create program is error");
        }
        return program;
    }

    /**
     * 当glGetError被调用时，它要么会返回错误标记之一，要么返回无错误。glGetError会返回的错误值如下：
     * 标记 	代码 	描述
     * GL_NO_ERROR 	0 	自上次调用glGetError以来没有错误
     * GL_INVALID_ENUM 	1280 	枚举参数不合法
     * GL_INVALID_VALUE 	1281 	值参数不合法
     * GL_INVALID_OPERATION 	1282 	一个指令的状态对指令的参数不合法
     * GL_STACK_OVERFLOW 	1283 	压栈操作造成栈上溢(Overflow)
     * GL_STACK_UNDERFLOW 	1284 	弹栈操作时栈在最低点（译注：即栈下溢(Underflow)）
     * GL_OUT_OF_MEMORY 	1285 	内存调用操作无法调用（足够的）内存
     * GL_INVALID_FRAMEBUFFER_OPERATION 	1286 	读取或写入一个不完整的帧缓冲     *
     *
     * @param op
     */
    public static void checkGlError(String op) {
        //当你不正确使用OpenGL的时候（比如说在绑定之前配置一个缓冲），它会检测到，并在幕后生成一个或多个用户错误标记。
        // 我们可以使用一个叫做glGetError的函数查询这些错误标记。，他会检测错误标记集，并且在OpenGL确实出错的时候返回一个错误值。
        int error = GLES30.glGetError();
        Log.i(TAG, "check gl error info: {" + error + "}");
        if (error != GLES30.GL_NO_ERROR) {
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    @DebugLog
    public static void initTexParams() {
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S,
                GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T,
                GLES30.GL_CLAMP_TO_EDGE);
    }

    public static void openGLEnableSetting() {
        //混合功能，将片元颜色和颜色缓冲区的颜色进行混合，参考glBlendFunc。
        GLES30.glEnable(GLES30.GL_BLEND);
        //在写入颜色缓冲区之前抖动颜色组件。
        // https://blog.csdn.net/GrimRaider/article/details/7449278
        GLES30.glEnable(GLES30.GL_DITHER);
        GLES30.glEnable(GLES30.GL_ALPHA_BITS);
        //https://learnopengl.com/Advanced-OpenGL/Anti-Aliasing
        GLES30.glEnable(GLES10.GL_MULTISAMPLE);//32925
        GLES30.glEnable(GLES10.GL_SMOOTH);//7425
        GLES30.glEnable(GLES10.GL_LINE_SMOOTH);//2848
        GLES30.glEnable(GLES10.GL_POINT_SMOOTH);//2832

        GLES30.glHint(GLES10.GL_POLYGON_SMOOTH_HINT, GLES10.GL_NICEST);//3155,4354
        // Make round points, not square points
        GLES30.glHint(GLES10.GL_POINT_SMOOTH_HINT, GLES10.GL_NICEST);//3153
    }
}
