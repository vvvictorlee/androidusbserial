package com.example.jack8.floatwindow;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jack8.floatwindow.AShCalculator.AShCalculator;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.jack8.floatwindow.Window.WindowStruct;
import com.kongqw.serialportlibrary.Device;
import com.kongqw.serialportlibrary.SerialPortFinder;
import com.kongqw.serialportlibrary.SerialPortManager;
import com.kongqw.serialportlibrary.listener.OnOpenSerialPortListener;
import com.kongqw.serialportlibrary.listener.OnSerialPortDataListener;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import me.zhouzhuo810.okusb.USB;

/**
 * 初始化視窗內容
 */
public class Calculator implements WindowStruct.constructionAndDeconstructionWindow, OnOpenSerialPortListener {
    WindowStruct windowStruct;
    Context context;
    View pageView;
    private USB usb;
    private static final String TAG = Calculator.class.getSimpleName();
    private SerialPortManager mSerialPortManager;
    /**
     * 初始化視窗子頁面內容
     *
     * @param context      視窗所在的Activity或Service的Context
     * @param pageView     子頁面的View
     * @param position     表示是第幾個子頁面
     * @param args         初始化視窗用的參數
     * @param windowStruct 子頁面所在的視窗本體
     */
    public void Construction(Context context, View pageView, int position, Object[] args, WindowStruct windowStruct) {
        switch (position) {
            case 0:
                calculator(context, pageView, windowStruct);
                break;
//            case 1:
//                initWindow2(context,pageView,windowStruct);
//                break;
//            case 2:
//                initWindow3(context,pageView,windowStruct);
//                break;
        }
    }

    public static boolean isNumber(String str) {
        String reg = "^[0-9]+(.[0-9])?$";
        return str.matches(reg);
    }
    String prevMax = "40";
    String prevMin = "30";
    static final float STEP_VALUE = .5f;
    static final float MAX_LIMIT_VALUE = 100.0f;
    static final float MIN_LIMIT_VALUE = .0f;

    void stepMax(float step) {
        float fmin = TemperatureParameters.getMinLimit(context);
        float cmax = TemperatureParameters.getMaxLimit(context);
        cmax += step;
        if (fmin >= cmax) {
            return;
        }
        else if(fmin>=cmax+step)
        {
//            pageView.findViewById(R.id.maxinc).setEnabled(false);
             pageView.findViewById(R.id.maxdec).setEnabled(false);

//            pageView.findViewById(R.id.mininc).setEnabled(false);
//            pageView.findViewById(R.id.mindec).setEnabled(false);
        }
        else
        {
            pageView.findViewById(R.id.maxdec).setEnabled(true);
            pageView.findViewById(R.id.mininc).setEnabled(true);
        }

        if(cmax>MAX_LIMIT_VALUE-STEP_VALUE)
        {
            pageView.findViewById(R.id.maxinc).setEnabled(false);
        }

        final EditText editTextMax = pageView.findViewById(R.id.editTextMax);
        editTextMax.setText(String.valueOf(cmax));
        TemperatureParameters.setMaxLimit(editTextMax.getContext(), cmax);
    }

    void stepMin(float step)
    {
        float fmax = TemperatureParameters.getMaxLimit(context);
        float cmin = TemperatureParameters.getMinLimit(context);
        cmin +=step;
        if(cmin>=fmax)
        {
           return;
        }
        else if(cmin+step>=fmax)
        {
//            pageView.findViewById(R.id.maxinc).setEnabled(false);
//            pageView.findViewById(R.id.maxdec).setEnabled(false);
            pageView.findViewById(R.id.mininc).setEnabled(false);
//            pageView.findViewById(R.id.mindec).setEnabled(false);
        }
        else
        {
            pageView.findViewById(R.id.maxdec).setEnabled(true);
            pageView.findViewById(R.id.mininc).setEnabled(true);
        }

        if(cmin<MIN_LIMIT_VALUE+STEP_VALUE)
    {
            pageView.findViewById(R.id.mindec).setEnabled(false);
    }


        final EditText editTextMin = pageView.findViewById(R.id.editTextMin);
        editTextMin.setText(String.valueOf(cmin));
        TemperatureParameters.setMinLimit(editTextMin.getContext(),cmin);
    }

    AShCalculator aShCalculato = new AShCalculator();

    void calculator(final Context context, final View pageView, final WindowStruct windowStruct){
        this.context = context;
        this.windowStruct=windowStruct;
        this.pageView = pageView;
        final EditText editText = pageView.findViewById(R.id.editText);
        final EditText editTextMax = pageView.findViewById(R.id.editTextMax);
        final EditText editTextMin = pageView.findViewById(R.id.editTextMin);
        editTextMax.setText(String.valueOf(TemperatureParameters.getMaxLimit(context)));
        editTextMin.setText(String.valueOf(TemperatureParameters.getMinLimit(context)));

        final EditText editTextTest = pageView.findViewById(R.id.editTextTest);
        final TextView tv = pageView.findViewById(R.id.textViewTest);
        String ss = editTextTest.getText().toString();
        tv.setText(ss);
        windowStruct.setWindowTitle(0,ss+"度");

//        ViewPager viewPager = pageView.findViewById(R.id.viewPager);
//        final View[] page = new View[2];
//        final ImageView[] pageIndicatorLight = new ImageView[]{
//                pageView.findViewById(R.id.pageindicatorLight1),
//                pageView.findViewById(R.id.pageindicatorLight2)
//        };


        final int sd = ContextCompat.getColor(context, com.jack8.floatwindow.R.color.windowTitleColor);
        final int sd1 = ContextCompat.getColor(context, com.jack8.floatwindow.R.color.windowTitleColor1);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.test) {
                    float min = TemperatureParameters.getMinLimit(v.getContext());
                    float max = TemperatureParameters.getMaxLimit(v.getContext());
                    String ss = editTextTest.getText().toString();
                    tv.setText(ss);
                    windowStruct.setWindowTitle(0, ss + "度");
                    float t = Float.parseFloat(ss);
                    int color = sd;
                    if (t > max) {
                        color = sd1;
                        tv.setText("1ss");
                    } else if (t < min) {
                        color = 0x0000FF;
                        tv.setText("2ss");
                    }
                    windowStruct.getWindowForm().setWindowTitleBackgroundColor(color);

                    return;
                }

                switch (v.getId()) {
                    case R.id.maxinc: {
                        stepMax(STEP_VALUE);
                        break;
                    }
                    case R.id.maxdec: {
                        stepMax(-STEP_VALUE);
                        break;
                    }
                    case R.id.mininc: {
                        stepMin(STEP_VALUE);
                        break;
                    }
                    case R.id.mindec: {
                        stepMin(-STEP_VALUE);
                        break;
                    }
                    default: {
                        break;
                    }
                }
                switch (v.getTag().toString()) {

                    case "=": {
                        String value;
                        try {
                            value = aShCalculato.exec(editText.getText().toString());
                        } catch (Exception e) {
                            value = e.getMessage();
                        }
                        editText.setText(value);
                        editText.setSelection(0, value.length());
                        break;
                    }
                    case "del": {
                        openPort();
                        //                        CharSequence value = editText.getText();
//                        if(value.length() != 0) {
//                            if(editText.getSelectionStart() < editText.getSelectionEnd())
//                                ((Editable) value).delete(editText.getSelectionStart(), editText.getSelectionEnd());
//                            else if(editText.getSelectionStart() != 0)
//                                ((Editable) value).delete(editText.getSelectionStart() - 1, editText.getSelectionStart());
//                        }
//                        if(value.length() == 0) {
//                            ((Editable) value).append('0');
//                            editText.setSelection(editText.getText().length());
//                        }
                        break;
                    }
                    default: {
//                        if(!editText.getText().toString().equals("0") || v.getTag().toString().equals("."))
//                            editText.getText().insert(editText.getSelectionStart(), v.getTag().toString());
//                        else {
//                            editText.setText(v.getTag().toString());
//                            editText.setSelection(editText.getText().length());
//                        }
                        break;
                    }
                }
            }
        };

        pageView.findViewById(R.id.del).setOnClickListener(onClickListener);
        pageView.findViewById(R.id.test).setOnClickListener(onClickListener);
        pageView.findViewById(R.id.maxinc).setOnClickListener(onClickListener);
        pageView.findViewById(R.id.maxdec).setOnClickListener(onClickListener);
        pageView.findViewById(R.id.mininc).setOnClickListener(onClickListener);
        pageView.findViewById(R.id.mindec).setOnClickListener(onClickListener);

        editTextMax.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //输入文字前触发
                prevMax=editTextMax.getText().toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //text改变过程中，一般在此加入监听事件。
            }

            @Override
            public void afterTextChanged(Editable s) {
                //输入后触发
                float fmax = TemperatureParameters.getMaxLimit(context);
                prevMax = String.valueOf(fmax);
         String ss = editTextMax.getText().toString();
                      tv.setText(ss);
                      if(ss.isEmpty())
                      {
                          editTextMax.setText(prevMax);
                          return;
                      }
                      if(!isNumber(ss))
                      {
                          editTextMax.setText(prevMax);
                          return;
                      }

                float fmin = TemperatureParameters.getMinLimit(context);
                float cmax = Float.parseFloat(ss);
                if(fmin>=cmax)
                {
                    Toast toast = Toast.makeText(editTextMax.getContext(), "上限不允许小于下限", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    editTextMax.setText(prevMax);
                    return;
                }

                if(fmin+STEP_VALUE<cmax)
                {
                    pageView.findViewById(R.id.maxdec).setEnabled(true);
                    pageView.findViewById(R.id.mininc).setEnabled(true);
                }

                if(cmax<MAX_LIMIT_VALUE-STEP_VALUE)
                {
                    pageView.findViewById(R.id.maxinc).setEnabled(true);
                }

                TemperatureParameters.setMaxLimit(editTextMax.getContext(),cmax);

            }
        });

        editTextMin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //输入文字前触发
                prevMin=editTextMin.getText().toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //text改变过程中，一般在此加入监听事件。
            }

            @Override
            public void afterTextChanged(Editable s) {
                //输入后触发
                float fmin = TemperatureParameters.getMinLimit(context);
                prevMin=String.valueOf(fmin);
                String ss = editTextMin.getText().toString();
                tv.setText(ss);
                if(ss.isEmpty())
                {
                    editTextMin.setText(prevMin);
                    return;
                }
                if(!isNumber(ss))
                {
                    editTextMin.setText(prevMin);
                    return;
                }


                float fmax = TemperatureParameters.getMaxLimit(context);
                float cmin = Float.parseFloat(ss);
                if(cmin>=fmax)
                {
                    Toast toast = Toast.makeText(editTextMin.getContext(), "下限不允许大于等于上限", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    editTextMin.setText(prevMin);
                    return;
                }

                if(cmin+STEP_VALUE<fmax)
                {
                    pageView.findViewById(R.id.maxdec).setEnabled(true);
                    pageView.findViewById(R.id.mininc).setEnabled(true);
                }

                if(cmin>MIN_LIMIT_VALUE+STEP_VALUE)
                {
                    pageView.findViewById(R.id.mindec).setEnabled(true);
                }

                TemperatureParameters.setMinLimit(editTextMin.getContext(),cmin);
            }
        });

//        page[0] = LayoutInflater.from(context).inflate(R.layout.calculator_sub_page1, viewPager, false);
//        loadCalculatorSubPage(page[0], onClickListener);
//        page[1] = LayoutInflater.from(context).inflate(R.layout.calculator_sub_page2, viewPager, false);
//        loadCalculatorSubPage(page[1], onClickListener);

//        viewPager.setAdapter(new PagerAdapter() {
//            @Override
//            public int getCount() {
//                return page.length;
//            }
//
//            @Override
//            public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
//                return view == o;
//            }
//
//            @Override
//            public void destroyItem(ViewGroup container, int position, Object object) {
//                container.removeView(page[position]);
//            }
//
//            @Override
//            public Object instantiateItem(ViewGroup container, int position) {
//                container.addView(page[position]);
//                return page[position];
//            }
//        });
//        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int i, float v, int i1) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                for(int i = 0;i < pageIndicatorLight.length;i++)
//                    if(i == position)
//                        pageIndicatorLight[i].setImageResource(R.drawable.page_indicator_focused);
//                    else
//                        pageIndicatorLight[i].setImageResource(R.drawable.page_indicator_unfocused);
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int i) {
//
//            }
//        });
    }


    void openPort()
    {
        SerialPortFinder serialPortFinder = new SerialPortFinder();

        ArrayList<Device> devices = serialPortFinder.getDevices();
        final EditText editText = pageView.findViewById(R.id.editText);
        int index = Integer.parseInt(editText.getText().toString());

        final TextView tv = pageView.findViewById(R.id.textViewTest);
        String ss = "";
        for(Device d : devices)
        {
            ss += d.getName()+";";
        }
        tv.setText(ss);
        if(devices.isEmpty())
        {
            Toast toast = Toast.makeText(context, "no device", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
            tv.setText("no device");
            return;
        }
        Device device = devices.get(index);

        mSerialPortManager = new SerialPortManager();

        // 打开串口
        boolean openSerialPort = mSerialPortManager.setOnOpenSerialPortListener(this)
                .setOnSerialPortDataListener(new OnSerialPortDataListener() {
                    @Override
                    public void onDataReceived(byte[] bytes) {
                        Log.i(TAG, "onDataReceived [ byte[] ]: " + Arrays.toString(bytes));
                        Log.i(TAG, "onDataReceived [ String ]: " + new String(bytes));
                        final byte[] finalBytes = bytes;
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                showToast(String.format("接收\n%s", new String(finalBytes)));
//                            }
//                        });
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final TextView tv = pageView.findViewById(R.id.textViewTest);
                                tv.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String s = new String(finalBytes);
                                        tv.setText(s);
                                        windowStruct.setWindowTitle(0, s + "度");
                                        final int sd = ContextCompat.getColor(context, com.jack8.floatwindow.R.color.windowTitleColor);
                                        final int sd1 = ContextCompat.getColor(context, com.jack8.floatwindow.R.color.windowTitleColor1);

                                                    float min = TemperatureParameters.getMinLimit(context);
                                                    float max = TemperatureParameters.getMaxLimit(context);
                                                    float t = Float.parseFloat(s);
                                                    int color = sd;
                                                    if (t > max) {
                                                        color = sd1;
                                                    } else if (t < min) {
                                                        color = 0x0000FF;
                                                    }
                                                    windowStruct.getWindowForm().setWindowTitleBackgroundColor(color);
                                        showToast(String.format("接收\n%s", s));
                                    }
                                });
                            }
                        }).start();
                    }

                    @Override
                    public void onDataSent(byte[] bytes) {
                        Log.i(TAG, "onDataSent [ byte[] ]: " + Arrays.toString(bytes));
                        Log.i(TAG, "onDataSent [ String ]: " + new String(bytes));
                        final byte[] finalBytes = bytes;
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                showToast(String.format("发送\n%s", new String(finalBytes)));
//                            }
//                        });
                    }
                })
                .openSerialPort(device.getFile(), 9600);


        Log.i(TAG, "onCreate: openSerialPort = " + openSerialPort);
    }
    /**
     * Toast
     *
     * @param content content
     */
    private void showToast(String content) {
        Toast mToast = null;
        if (null == mToast) {
            mToast = Toast.makeText(context.getApplicationContext(), null, Toast.LENGTH_SHORT);
        }
        mToast.setText(content);
        mToast.show();
    }
void openUsbSerial()
{
    usb = new USB.USBBuilder( (Activity)context )
            .setBaudRate(9600)
            .setDataBits(8)
            .setStopBits(UsbSerialPort.STOPBITS_1)
            .setParity(UsbSerialPort.PARITY_NONE)
            .setMaxReadBytes(20)
            .setReadDuration(500)
            .setDTR(false)
            .setRTS(false)
            .build();


    usb.setOnUsbChangeListener(new USB.OnUsbChangeListener() {
        @Override
        public void onUsbConnect() {
            Toast.makeText(context, "conencted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onUsbDisconnect() {
            Toast.makeText(context, "disconencted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDataReceive(byte[] data) {
//                final String rData = DataUtil.byteArrayToString(data);
//                tvResult.append(rData);
//            tvResult.setText(new String(data, Charset.forName("GB2312")));
        }

        @Override
        public void onUsbConnectFailed() {
            Toast.makeText(context, "connect fail", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionGranted() {
            Toast.makeText(context, "permission ok", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionRefused() {
            Toast.makeText(context, "permission fail", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDriverNotSupport() {
            Toast.makeText(context, "no driver", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onWriteDataFailed(String error) {
            Toast.makeText(context, "write fail" + error, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onWriteSuccess(int num) {
            Toast.makeText(context, "write ok " + num, Toast.LENGTH_SHORT).show();
        }
    });
}
//    private void loadCalculatorSubPage(View page1, View.OnClickListener onClickListener){
//        TableLayout tableLayout = page1.findViewById(R.id.table);
//        for(int i = 0;i <tableLayout.getChildCount();i++){
//            TableRow tableRow = (TableRow)tableLayout.getChildAt(i);
//            for(int j = 0;j < tableRow.getChildCount();j++){
//                tableRow.getChildAt(j).setOnClickListener(onClickListener);
//            }
//        }
//    }
//
//    private AdView adView1;
//    void initWindow2(Context context, View pageView, final WindowStruct windowStruct){
//        final EditText et=(EditText)pageView.findViewById(R.id.Temperature);
//        View.OnClickListener oc=new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(et.getText().toString().matches("^\\s*$"))
//                    return;
//                switch (v.getId()) {
//                    case R.id.toC:
//                        et.setText(String.valueOf((Float.parseFloat(et.getText().toString()) - 32) * 5f / 9f));
//                        break;
//                    case R.id.toF:
//                        et.setText(String.valueOf(Float.parseFloat(et.getText().toString())*(9f/5f)+32));
//                        break;
//                }
//            }
//        };
//        ((Button)pageView.findViewById(R.id.toC)).setOnClickListener(oc);
//        ((Button)pageView.findViewById(R.id.toF)).setOnClickListener(oc);
//        MobileAds.initialize(context, context.getString(R.string.AD_ID));
//        adView1 = pageView.findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("6B58CCD0570D93BA1317A64BEB8BA677")
//                .addTestDevice("1E461A352AC1E22612B2470A43ADADBA")
//                .addTestDevice("F4734F4691C588DB93799277888EA573")
//                .build();
//        adView1.loadAd(adRequest);
//        adView1.pause();
//    }
//
//    private AdView adView2;
//    void initWindow3(Context context, View pageView, final WindowStruct windowStruct){
//        final EditText H=(EditText)pageView.findViewById(R.id.H),W=(EditText)pageView.findViewById(R.id.W);
//        final TextView BMI=(TextView)pageView.findViewById(R.id.BMI);
//        ((Button)pageView.findViewById(R.id.CH)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(H.getText().toString().matches("^\\s*$")||W.getText().toString().matches("^\\s*$"))
//                    return;
//                float h=Float.parseFloat(H.getText().toString())/100f;
//                BMI.setText(String.valueOf(Float.parseFloat(W.getText().toString())/(h*h)));
//            }
//        });
//        adView2 = pageView.findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("6B58CCD0570D93BA1317A64BEB8BA677")
//                .addTestDevice("1E461A352AC1E22612B2470A43ADADBA")
//                .addTestDevice("F4734F4691C588DB93799277888EA573")
//                .build();
//        adView2.loadAd(adRequest);
//        adView2.pause();
//    }

    public void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct){
//        if(position == 0)
//            adView1.destroy();
//        else if(position == 1)
//            adView2.destroy();
    }

    @Override
    public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {
//        if(position == 0)
//            adView1.resume();
//        else if(position == 1)
//            adView2.resume();
    }

    @Override
    public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {
//        if(position == 0)
//            adView1.pause();
//        else if(position == 1)
//            adView2.pause();
    }

    @Override
    public void onSuccess(File device) {
        Toast toast = Toast.makeText(context, "open ok:"+device.getName(), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    @Override
    public void onFail(File device, Status status) {
        Toast toast = Toast.makeText(context, "open fail:"+device.getName(), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }
}
