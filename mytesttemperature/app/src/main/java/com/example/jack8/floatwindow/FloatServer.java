package com.example.jack8.floatwindow;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;


import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jack8.floatwindow.Window.WindowStruct;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * 浮動視窗服務
 */
public class FloatServer extends Service {
    public static final int OPEN_NONE = 0x0000;
    public static final int OPEN_FLOAT_WINDOW = 0x0001;
    public static final int OPEN_EXTRA_URL = 0x0002;
    public static final int SHOW_WINDOW_MANAGER = 0x0004;
    public static final int SHOW_FLOAT_WINDOW_MENU = 0x0008;
    public static final int OPEN_WEB_BROWSER = 0x0010;
    public static final int OPEN_NOTE_PAGE = 0x0020;
    public static final int OPEN_CALCULATO = 0x0040;
    public static final int OPEN_MAIN_MENU = 0x0080;
    public static final int OPEN_SETTING = 0x0100;
    public static final int SHOW_CLOSE_FLOAT_WINDOW = 0x0200;
    public static final int SHOW_WATCHED_AD = 0x0400;
    public static final String LAUNCHER = "launcher";
    public static final String INTENT = "intent";
    public static final String EXYRA_URL = "extra_url";

    private static final String BCAST_CONFIGCHANGED ="android.intent.action.CONFIGURATION_CHANGED";

    static int wm_count=0;//計算FloatServer總共開了多少次

    WindowManager wm;
    Notification NF;
    final int NOTIFY_ID=851262;
    final String NOTIFY_CHANNEL_ID = "FloatWindow";
    HashMap<Integer, WindowStruct> windowList;
    WindowStruct windowManager = null;//視窗管理員
    WindowStruct menu = null;
    WindowStruct help = null;
    Handler handler = new Handler();
    WindowStruct.WindowAction windowAction = new WindowStruct.WindowAction() {
        @Override
        public void goHide(WindowStruct windowStruct) {

        }

        @Override
        public void goClose(WindowStruct windowStruct) {
            if (--wm_count == 0) {
                if(!WindowParameter.isPermanent(FloatServer.this))
                    closeFloatWindow();
            }
        }
    };

    private void closeFloatWindow(){
        this.stopForeground(true);
        try {
            unregisterReceiver(ScreenChangeListener.getInstance(this));
        }catch (IllegalArgumentException e){
            Crashlytics.logException(e);
        }
        try {
            unregisterReceiver(HomeKeyListener.getInstance(this));
        }catch (IllegalArgumentException e){
            Crashlytics.logException(e);
        }
        stopSelf();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        JackLog.setWriteLogDrive(this,
                "d936f0197b7e6c67"
        );
        wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notify_view);
        remoteViews.setOnClickPendingIntent(R.id.web_browser,
                PendingIntent.getActivity(this,
                        0,
                        new Intent(this, WebBrowserLauncher.class),
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
        );
        remoteViews.setOnClickPendingIntent(R.id.note,
                PendingIntent.getActivity(this,
                        1,
                        new Intent(this, NotePageLauncher.class),
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
        );
        remoteViews.setOnClickPendingIntent(R.id.calculato,
                PendingIntent.getActivity(this,
                        2,
                        new Intent(this, CalculatorLauncher.class),
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
        );
        remoteViews.setOnClickPendingIntent(R.id.setting,
                PendingIntent.getActivity(this,
                        3,
                        new Intent(this, Setting.class),
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
        );
        remoteViews.setOnClickPendingIntent(R.id.window_list,
                PendingIntent.getService(this,
                        4,
                        new Intent(this,FloatServer.class).putExtra(INTENT,SHOW_WINDOW_MANAGER),
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
        );
        remoteViews.setOnClickPendingIntent(R.id.close,
                PendingIntent.getService(this,
                        5,
                        new Intent(this,FloatServer.class).putExtra(INTENT,SHOW_CLOSE_FLOAT_WINDOW),
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
        );

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            NotificationCompat.Builder NFB = new NotificationCompat.Builder(this);
            NFB.setSmallIcon(R.drawable.mini_window).
                    setContentTitle(getString(R.string.app_name)).
                    setContent(remoteViews);
//                    addAction(new NotificationCompat.Action.Builder(R.drawable.settings, getString(R.string.setting), PendingIntent.getActivity(this, 0, toSetup, PendingIntent.FLAG_UPDATE_CURRENT)).build()).
//                    addAction(new NotificationCompat.Action.Builder(R.drawable.menu, getString(R.string.windows_list), PendingIntent.getService(this, 1, showWindowManager, PendingIntent.FLAG_UPDATE_CURRENT)).build()).
//                    setContentText(getString(R.string.runing)).
//                    setContentIntent(PendingIntent.getService(this, 0, showFloatWindowMenu, PendingIntent.FLAG_UPDATE_CURRENT));
            NF = NFB.build();
            startForeground(NOTIFY_ID, NF);//將服務升級至前台等級，這樣就不會突然被系統回收
        }else{
            NotificationChannel NC = new NotificationChannel(NOTIFY_CHANNEL_ID,getString(R.string.app_name),NotificationManager.IMPORTANCE_LOW);
            NC.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(NC);

            Notification.Builder NFB = new Notification.Builder(this,NOTIFY_CHANNEL_ID);
            NFB.setSmallIcon(R.drawable.mini_window).
                    setContentTitle(getString(R.string.app_name)).
                    setCustomContentView(remoteViews);
                    //addAction(new Notification.Action.Builder(R.drawable.settings, getString(R.string.setting), PendingIntent.getActivity(this, 0, toSetup, PendingIntent.FLAG_UPDATE_CURRENT)).build()).
                    //addAction(new Notification.Action.Builder(R.drawable.menu, getString(R.string.windows_list), PendingIntent.getService(this, 1, showWindowManager, PendingIntent.FLAG_UPDATE_CURRENT)).build()).
                    //setContentText(getString(R.string.runing))
                    //setContentIntent(PendingIntent.getService(this, 0, showFloatWindowMenu, PendingIntent.FLAG_UPDATE_CURRENT));
            NF = NFB.build();
            startForeground(NOTIFY_ID, NF);//將服務升級至前台等級，這樣就不會突然被系統回收
        }
        Log.i("WMStrver","Create");

        //---------------註冊翻轉事件廣播接收---------------
        IntentFilter filter = new IntentFilter();
        filter.addAction(BCAST_CONFIGCHANGED);
        this.registerReceiver(ScreenChangeListener.getInstance(this), filter);
        //-------------------------------------------------
        //---------------註冊Home鍵廣播接收---------------
        this.registerReceiver(HomeKeyListener.getInstance(this), new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        //-------------------------------------------------

        try {//用反射取得所有視窗清單
            Field field = WindowStruct.class.getDeclaredField("windowList");
            field.setAccessible(true);
            windowList = (HashMap<Integer,WindowStruct>)field.get(WindowStruct.class);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /*
    關於onStartCommand的說明
    http://www.cnblogs.com/not-code/archive/2011/05/21/2052713.html
     */
    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        int initCode = intent.getIntExtra(INTENT,OPEN_NONE);
        if((initCode & OPEN_MAIN_MENU) == OPEN_MAIN_MENU) {
            wm_count++;
            new WindowStruct.Builder(this,wm)
                    .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.MAX_BUTTON | WindowStruct.MINI_BUTTON | WindowStruct.CLOSE_BUTTON | WindowStruct.SIZE_BAR)
                    .windowPages(new int[]{R.layout.main_menu})
                    .windowPageTitles(new String[]{getResources().getString(R.string.app_name)})
                    .windowInitArgs(new Object[1][0])
                    .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                    .heightAndTopAutoCenter((int)(getResources().getDisplayMetrics().density * 320))
                    .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                    .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                    .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
                    .windowAction(windowAction)
                    .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                        AdView adView;
                        @Override
                        public void Construction(Context context, View pageView, int position, Object[] args, final WindowStruct windowStruct) {
                            View.OnClickListener onClickListener = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Class clazz = null;
                                    switch (v.getId()){
                                        case R.id.web_browser:{
                                            clazz = WebBrowserLauncher.class;
                                            break;
                                        }
                                        case R.id.note:{
                                            clazz = NotePageLauncher.class;
                                            break;
                                        }
                                        case R.id.calculato:{
                                            clazz = CalculatorLauncher.class;
                                            break;
                                        }
                                        case R.id.setting:{
                                            clazz = Setting.class;
                                            break;
                                        }
                                        case R.id.watch_ad:{
                                            clazz = HelpMeAd.class;
                                            break;
                                        }
                                    }
                                    Intent intent = new Intent(FloatServer.this, clazz);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    FloatServer.this.startActivity(intent);
                                    windowStruct.close();
                                }
                            };
                            pageView.findViewById(R.id.web_browser).setOnClickListener(onClickListener);
                            pageView.findViewById(R.id.note).setOnClickListener(onClickListener);
                            pageView.findViewById(R.id.calculato).setOnClickListener(onClickListener);
                            pageView.findViewById(R.id.setting).setOnClickListener(onClickListener);
                            pageView.findViewById(R.id.watch_ad).setOnClickListener(onClickListener);
                            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
                                pageView.findViewById(R.id.tip).setVisibility(View.VISIBLE);
                                View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(final View v) {
                                        ListView menu_list = new ListView(FloatServer.this);
                                        menu_list.setAdapter(new ArrayAdapter<String>(FloatServer.this, R.layout.list_item, R.id.item_text, new String[]{getResources().getString(R.string.add_to_home_screen)}));
                                        menu_list.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                                        final PopupWindow popupWindow =new PopupWindow(FloatServer.this);
                                        popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
                                        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                                        popupWindow.setContentView(menu_list);
                                        popupWindow.setFocusable(true);
                                        int anchorLoc[] = new int[2];
                                        v.getLocationInWindow(anchorLoc);
                                        popupWindow.showAtLocation(v, Gravity.LEFT | Gravity.TOP,0, anchorLoc[1]);
                                        menu_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                int name = 0, R_icon = 0;
                                                Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT"),
                                                        launcher = new Intent(getApplicationContext() , MainActivity.class);
                                                switch (v.getId()){
                                                    case R.id.web_browser:{
                                                        name = R.string.web_browser;
                                                        R_icon = R.drawable.browser;
                                                        launcher.putExtra(LAUNCHER, OPEN_WEB_BROWSER);
                                                        break;
                                                    }
                                                    case R.id.note:{
                                                        name = R.string.note;
                                                        R_icon = R.drawable.note;
                                                        launcher.putExtra(LAUNCHER, OPEN_NOTE_PAGE);
                                                        break;
                                                    }
                                                    case R.id.calculato:{
                                                        name = R.string.calculator;
                                                        R_icon = R.drawable.calculator;
                                                        launcher.putExtra(LAUNCHER, OPEN_CALCULATO);
                                                        break;
                                                    }
                                                    case R.id.setting:{
                                                        name = R.string.setting;
                                                        R_icon = R.drawable.setting_icon;
                                                        launcher.putExtra(LAUNCHER, OPEN_SETTING);
                                                        break;
                                                    }
                                                }
                                                //shortcutIntent.putExtra("duplicate", false);//是否可以重複建立
                                                shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(name));
                                                Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R_icon);
                                                shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
                                                shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcher);
                                                sendBroadcast(shortcutIntent);
                                                popupWindow.dismiss();
                                                Toast.makeText(FloatServer.this,getString(R.string.added_to_the_home_screen),Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        return true;
                                    }
                                };
                                pageView.findViewById(R.id.web_browser).setOnLongClickListener(onLongClickListener);
                                pageView.findViewById(R.id.note).setOnLongClickListener(onLongClickListener);
                                pageView.findViewById(R.id.calculato).setOnLongClickListener(onLongClickListener);
                                pageView.findViewById(R.id.setting).setOnLongClickListener(onLongClickListener);
                            }
                            adView = pageView.findViewById(R.id.adView);
                            AdRequest adRequest = new AdRequest.Builder()
                                    .addTestDevice("6B58CCD0570D93BA1317A64BEB8BA677")
                                    .addTestDevice("1E461A352AC1E22612B2470A43ADADBA")
                                    .addTestDevice("F4734F4691C588DB93799277888EA573")
                                    .build();
                            adView.loadAd(adRequest);

                            Button helpButton = new Button(context);
                            helpButton.setLayoutParams(new ViewGroup.LayoutParams(windowStruct.getWindowButtonsWidth(), windowStruct.getWindowButtonsHeight()));
                            helpButton.setPadding(0,0,0,0);
                            helpButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.help));
                            helpButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(help == null) {
                                        wm_count++;
                                        help = new WindowStruct.Builder(FloatServer.this, wm)
                                                .windowPages(new int[]{R.layout.what_is_new, R.layout.help})
                                                .windowPageTitles(new String[]{getResources().getString(R.string.new_functions), getResources().getString(R.string.help)})
                                                .transitionsDuration(WindowParameter.getWindowTransitionsDuration(FloatServer.this))
                                                .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(FloatServer.this)))
                                                .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(FloatServer.this)))
                                                .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(FloatServer.this)))
                                                .windowAction(new WindowStruct.WindowAction() {
                                                    @Override
                                                    public void goHide(WindowStruct windowStruct) {

                                                    }

                                                    @Override
                                                    public void goClose(WindowStruct windowStruct) {
                                                        help = null;
                                                        windowAction.goClose(windowStruct);
                                                    }
                                                })
                                                .constructionAndDeconstructionWindow(new Help())
                                                .show();
                                    }else
                                        help.focusAndShowWindow();
                                }
                            });
                            ViewGroup micro_max_button = pageView.getRootView().findViewById(R.id.micro_max_button_background);
                            micro_max_button.addView(helpButton,0);
                        }

                        @Override
                        public void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct) {
                            adView.destroy();
                        }

                        @Override
                        public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {

                        }

                        @Override
                        public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {

                        }
                    })
                    .show();
        }else if((initCode & OPEN_WEB_BROWSER) == OPEN_WEB_BROWSER) {
            wm_count++;
            if((initCode & OPEN_EXTRA_URL) != OPEN_EXTRA_URL)
                new WindowStruct.Builder(this,wm)
                        .windowPages(new int[]{R.layout.webpage, R.layout.bookmark_page, R.layout.history_page})
                        .windowPageTitles(new String[]{getResources().getString(R.string.web_browser), getResources().getString(R.string.bookmarks), getResources().getString(R.string.history)})
                        .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                        .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                        .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                        .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
                        .windowAction(windowAction)
                        .constructionAndDeconstructionWindow(new WebBrowser())
                        .show();
            else{
                String extra_url = intent.getStringExtra(EXYRA_URL);
                new WindowStruct.Builder(this,wm)
                        .windowPages(new int[]{R.layout.webpage, R.layout.bookmark_page, R.layout.history_page})
                        .windowPageTitles(new String[]{getResources().getString(R.string.web_browser), getResources().getString(R.string.bookmarks), getResources().getString(R.string.history)})
                        .windowInitArgs(new Object[][]{new Object[]{extra_url}})
                        .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                        .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                        .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                        .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
                        .windowAction(windowAction)
                        .constructionAndDeconstructionWindow(new WebBrowser())
                        .show();
            }
        }else if((initCode & OPEN_NOTE_PAGE) == OPEN_NOTE_PAGE) {
            wm_count++;
            if((initCode & OPEN_EXTRA_URL) != OPEN_EXTRA_URL)
                new WindowStruct.Builder(this,wm)
                        .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.MAX_BUTTON | WindowStruct.MINI_BUTTON | WindowStruct.HIDE_BUTTON | WindowStruct.CLOSE_BUTTON | WindowStruct.SIZE_BAR)
                        .windowPages(new int[]{R.layout.note_page})
                        .windowPageTitles(new String[]{getResources().getString(R.string.note)})
                        .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                        .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                        .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                        .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
                        .windowAction(windowAction)
                        .constructionAndDeconstructionWindow(new NotePage())
                        .show();
            else{
                String extra_url = intent.getStringExtra(EXYRA_URL);
                new WindowStruct.Builder(this,wm)
                        .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.MAX_BUTTON | WindowStruct.MINI_BUTTON | WindowStruct.HIDE_BUTTON | WindowStruct.CLOSE_BUTTON | WindowStruct.SIZE_BAR)
                        .windowPages(new int[]{R.layout.note_page})
                        .windowPageTitles(new String[]{getResources().getString(R.string.note)})
                        .windowInitArgs(new Object[][]{new Object[]{NotePage.ADD_NOTE,extra_url}})
                        .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                        .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                        .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                        .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
                        .windowAction(windowAction)
                        .constructionAndDeconstructionWindow(new NotePage())
                        .show();
            }
        }else if((initCode & OPEN_CALCULATO) == OPEN_CALCULATO) {
            wm_count++;
            new WindowStruct.Builder(this,wm)
                    .windowPages(new int[]{ R.layout.calculator,R.layout.window_context, R.layout.window_conetxt2})
                    .windowPageTitles(new String[]{getResources().getString(R.string.calculator), getResources().getString(R.string.temperature_conversion), getResources().getString(R.string.BMI_conversion)})
                    .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.MINI_BUTTON )
                    .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                    .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                    .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                    .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
                    .height((int)(getResources().getDisplayMetrics().density * (269 + WindowParameter.getWindowButtonsHeight(this) + WindowParameter.getWindowSizeBarHeight(this))))
                    //.width((int)(getResources().getDisplayMetrics().density * 200))
                    .windowAction(windowAction)
                    .constructionAndDeconstructionWindow(new Calculator())
                    .show();
        }else if((initCode & SHOW_WATCHED_AD) == SHOW_WATCHED_AD) {
            View messageView = LayoutInflater.from(this).inflate(R.layout.alert, null);
            ((TextView)messageView.findViewById(R.id.message)).setText(getString(R.string.watched_ad));
            messageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            wm_count++;
            new WindowStruct.Builder(this, (WindowManager) this.getSystemService(Context.WINDOW_SERVICE))
                    .windowPageTitles(new String[]{getString(R.string.app_name)})
                    .windowPages(new View[]{messageView})
                    .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
                    .left((getResources().getDisplayMetrics().widthPixels / 2) - messageView.getMeasuredWidth() / 2)
                    .top((getResources().getDisplayMetrics().heightPixels / 2) - (messageView.getMeasuredHeight() + (int)(getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this))) / 2)
                    .width(messageView.getMeasuredWidth())
                    .height((messageView.getMeasuredHeight() + (int)(getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this))))
                    .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                    .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                    .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                    .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
                    .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                        @Override
                        public void Construction(Context context, View pageView, int position, Object[] args, final WindowStruct ws) {
                            pageView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ws.close();
                                }
                            });
                        }

                        @Override
                        public void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct1) {

                        }

                        @Override
                        public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {

                        }

                        @Override
                        public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {

                        }
                    })
                    .windowAction(windowAction)
                    .show();
        }else{
            //---------------------收起下拉選單-----------------------------
            try {
                Object statusBarManager = getSystemService("statusbar");
                Method collapse;

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                    collapse = statusBarManager.getClass().getMethod("collapse");
                } else {
                    collapse = statusBarManager.getClass().getMethod("collapsePanels");
                }
                collapse.invoke(statusBarManager);
            } catch (Exception localException) {
                localException.printStackTrace();
            }
            //-----------------------------------------------------------------------
            if((initCode & SHOW_FLOAT_WINDOW_MENU) == SHOW_FLOAT_WINDOW_MENU){
                ListView menuView = new ListView(this);
                menuView.setAdapter(new ArrayAdapter<String>(FloatServer.this,R.layout.hide_menu_item,R.id.item_text,new String[]{getString(R.string.setting),getString(R.string.windows_list)}));
                if(menu != null)
                    menu.focusAndShowWindow();
                else {
                    wm_count++;
                    menu = new WindowStruct.Builder(this, wm)
                            .windowPages(new View[]{menuView})
                            .windowPageTitles(new String[]{getString(R.string.app_name)})
                            .height((int) ((110 + WindowParameter.getWindowButtonsHeight(this)) * this.getResources().getDisplayMetrics().density))
                            .width((int) (200 * this.getResources().getDisplayMetrics().density))
                            .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
                            .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                            .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                            .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                            .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
                            .windowAction(new WindowStruct.WindowAction() {
                                @Override
                                public void goHide(WindowStruct windowStruct) {

                                }

                                @Override
                                public void goClose(WindowStruct windowStruct) {
                                    menu = null;
                                    windowAction.goClose(windowStruct);
                                }
                            })
                            .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                                @Override
                                public void Construction(Context context, View pageView, int position, Object[] args, WindowStruct windowStruct) {

                                }

                                @Override
                                public void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct) {

                                }

                                @Override
                                public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {

                                }

                                @Override
                                public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {

                                }
                            }).show();
                    menuView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            menu.close();
                            switch (position) {
                                case 0:
                                    Intent intent = new Intent(FloatServer.this, Setting.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    break;
                                case 1:
                                    showUnWindowMenu();
                            }
                        }
                    });
                }
            }else if((initCode & SHOW_WINDOW_MANAGER) == SHOW_WINDOW_MANAGER) {
                showUnWindowMenu();
            }else if((initCode & SHOW_CLOSE_FLOAT_WINDOW) == SHOW_CLOSE_FLOAT_WINDOW) {
                if(windowList.isEmpty())
                    closeFloatWindow();
                else{
                    View messageView = LayoutInflater.from(this).inflate(R.layout.alert, null);
                    ((TextView)messageView.findViewById(R.id.message)).setText(getString(R.string.close_notification_message));
                    messageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    wm_count++;
                    new WindowStruct.Builder(this, (WindowManager) this.getSystemService(Context.WINDOW_SERVICE))
                            .windowPageTitles(new String[]{getString(R.string.close_notification)})
                            .windowPages(new View[]{messageView})
                            .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
                            .left((getResources().getDisplayMetrics().widthPixels / 2) - messageView.getMeasuredWidth() / 2)
                            .top((getResources().getDisplayMetrics().heightPixels / 2) - (messageView.getMeasuredHeight() + (int)(getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this))) / 2)
                            .width(messageView.getMeasuredWidth())
                            .height((messageView.getMeasuredHeight() + (int)(getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this))))
                            .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                            .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                            .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                            .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
                            .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                                @Override
                                public void Construction(Context context, View pageView, int position, Object[] args, final WindowStruct ws) {
                                    pageView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            ws.close();
                                        }
                                    });
                                }

                                @Override
                                public void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct1) {

                                }

                                @Override
                                public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {

                                }

                                @Override
                                public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {

                                }
                            })
                            .windowAction(windowAction)
                            .show();
                }
            }
        }

        return START_NOT_STICKY;//START_REDELIVER_INTENT;
    }
    void showUnWindowMenu(){
        final ListView hideMenu=new ListView(this);
        final hideMenuAdapter hma = new hideMenuAdapter();
        hideMenu.setAdapter(hma);
        /*menu=new AlertDialog.Builder(this).setTitle("所有視窗清單").setView(hideMenu).create();
        menu.getWindow().setType((Build.VERSION.SDK_INT < Build.VERSION_CODES.O) ? WindowManager.LayoutParams.TYPE_SYSTEM_ALERT : WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        menu.show();
        hideMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //menu.dismiss();
                windowList.get(hma.key[position]).focusAndShowWindow();
            }
        });*/
        if(windowManager == null) {
            wm_count++;
            windowManager = new WindowStruct.Builder(this, wm)
                    .windowPages(new View[]{hideMenu})
                    .windowPageTitles(new String[]{getString(R.string.windows_list)})
                    .height((int) ((160 + WindowParameter.getWindowButtonsHeight(this) + WindowParameter.getWindowSizeBarHeight(this)) * this.getResources().getDisplayMetrics().density))
                    .width((int) (195 * this.getResources().getDisplayMetrics().density))
                    .displayObject(WindowStruct.SIZE_BAR | WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
                    .transitionsDuration(WindowParameter.getWindowTransitionsDuration(this))
                    .windowButtonsHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsHeight(this)))
                    .windowButtonsWidth((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowButtonsWidth(this)))
                    .windowSizeBarHeight((int) (getResources().getDisplayMetrics().density * WindowParameter.getWindowSizeBarHeight(this)))
                    .windowAction(new WindowStruct.WindowAction() {
                        @Override
                        public void goHide(WindowStruct windowStruct) {

                        }

                        @Override
                        public void goClose(WindowStruct windowStruct) {
                            windowManager = null;
                            windowAction.goClose(windowStruct);
                        }
                    })
                    .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                        @Override
                        public void Construction(Context context, View pageView, int position, Object[] args, WindowStruct windowStruct) {

                        }

                        @Override
                        public void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct) {

                        }

                        @Override
                        public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {

                        }

                        @Override
                        public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {

                        }
                    }).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int windowListLength = 0;
                    while (windowManager != null) {
                        if (windowListLength != windowList.size())
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    hma.updateWindowList();
                                    hma.notifyDataSetChanged();
                                }
                            });
                        windowListLength = windowList.size();
                        try {
                            Thread.sleep(1l);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }else
            windowManager.focusAndShowWindow();
    }

    class hideMenuAdapter extends BaseAdapter{
        private Integer[] key;

        public hideMenuAdapter(){
            updateWindowList();
        }

        public void updateWindowList(){
            key = windowList.keySet().toArray(new Integer[windowList.size()]);
        }

        @Override
        public int getCount() {
            return key.length;
        }

        @Override
        public Object getItem(int position) {
            return windowList.get(key[position]);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(FloatServer.this).inflate(R.layout.window_manager, null);
            if(windowList.containsKey(key[position])) {
                final WindowStruct windowStruct = windowList.get(key[position]);
                ((TextView) convertView.findViewById(R.id.title)).setText(windowStruct.getWindowTitle());
                convertView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        windowStruct.close();
                    }
                });
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        windowStruct.focusAndShowWindow();
                    }
                });
            }
            return convertView;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}